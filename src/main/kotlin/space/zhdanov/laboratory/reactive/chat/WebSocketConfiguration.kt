package space.zhdanov.laboratory.reactive.chat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.cors.CorsConfiguration



@Configuration
class WebSocketConfiguration @Autowired constructor( val webSocketHandler: MyWebSocketHandler) {

    @Bean
    fun wsha() = WebSocketHandlerAdapter()

    @Bean
    fun hm(): HandlerMapping {
        val suhm = SimpleUrlHandlerMapping()
        val cors = CorsConfiguration()
        cors.addAllowedOrigin("*")

        suhm.order = 10
        suhm.urlMap = mapOf("/chat" to webSocketHandler)
        suhm.setCorsConfigurations(mapOf("/chat" to cors))
        return suhm
    }

}