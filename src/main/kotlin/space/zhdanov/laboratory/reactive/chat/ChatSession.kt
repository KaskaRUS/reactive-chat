package space.zhdanov.laboratory.reactive.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.*


class ChatSession(private val processor: ReplayProcessor<Message>) {

    private val mapper = ObjectMapper().registerModule(KotlinModule())
    val connectedEvent = MonoProcessor.create<User>()
    val disconnectEvent = MonoProcessor.create<User>()

    private lateinit var session: WebSocketSession
    private lateinit var user: User

    fun handle(session: WebSocketSession): Mono<Void> {
        this.session = session

        val messages = processor.map { mapper.writeValueAsString(it) }.map { session.textMessage(it) }
        val sender = session.send(messages).subscribe()
        val mapper = jacksonObjectMapper()

        val receiver = session.receive().map { it.payloadAsText }
                .map { mapper.readValue<Message>(it) }
                .doOnNext {
                    if (it.type == TypeMessage.LOGIN) {
                        val obj = it.data as Map<*, *>
                        val u = users.find { it.name == obj["name"] && it.password == obj["password"] }
                        if (u == null)
                            sender.dispose()
                        else {
                            user = u
                            connectedEvent.onNext(u)
                        }
                    } else {
                        processor.onNext(it)
                    }
                }
                .doOnNext { println(it) }

        val disconnect = Mono
                .fromRunnable<Any> {
                    disconnectEvent.onNext(user)
                    sender.dispose()
                }

        return receiver.then(disconnect).then()
    }
}