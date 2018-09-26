package space.zhdanov.laboratory.reactive.chat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.ReplayProcessor

@Component
class MyWebSocketHandler: WebSocketHandler {

    private val messageProcessor = ReplayProcessor.create<Message>(20)

    override fun handle(session: WebSocketSession): Mono<Void> {

        val cookies = parseCookie(session.handshakeInfo.headers["Cookie"]?.get(0) ?: "")

        val name = cookies["login"] ?: ""
        val password = cookies["password"] ?: ""
        val user = users.first { it.name == name && it.password == password}
        val newSession = ChatSession(messageProcessor, user)

        newSession.connectedEvent.subscribe{
            users.find { it.name == it.name }.apply { it.online = true }
            messageProcessor.onNext(getUsersMessage(it))
        }

        newSession.disconnectEvent.subscribe {
            users.find { it.name == it.name }.apply { it.online = false }
            messageProcessor.onNext(getUsersMessage(it))
        }

        return newSession.handle(session)
    }

}

fun getUsersMessage(user: User) : Message =
        Message(user, users
                        .map { User(it.name, "", it.online) }
                        .sortedWith(compareByDescending { it.online }),
                TypeMessage.USERS)

fun parseCookie(cookie: String): Map<String, String> {
    val pairs = cookie.split(";")
    return pairs.map {
        val pair = it.trim().split("=")
        return@map pair[0] to pair[1]
    }.toMap()
}