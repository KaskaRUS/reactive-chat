package space.zhdanov.laboratory.reactive.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.ReplayProcessor


class ChatSession(val processor: ReplayProcessor<Message>) {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private lateinit var session: WebSocketSession

    fun handle(session: WebSocketSession): Mono<Void> {
        this.session = session

        val messages = processor.map { mapper.writeValueAsString(it) }.map { session.textMessage(it) }

        val sender = session.send(messages).subscribe()

        val mapper = jacksonObjectMapper()

        val receiver = session.receive().map { it.payloadAsText }
                .map { mapper.readValue<Message>(it) }
                .doOnNext { processor.onNext(it) }
                .doOnNext { println(it) }


        val connected = Mono
                .fromRunnable<Any> {
                    println("run")
                }

        val disconnect = Mono
                .fromRunnable<Any> {
                    println("disconnect")
                    sender.dispose()
                }

        return connected.thenMany(receiver).then(disconnect).then()
    }
}