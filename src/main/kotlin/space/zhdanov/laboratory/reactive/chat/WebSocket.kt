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
        val newSession = ChatSession(messageProcessor)

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
