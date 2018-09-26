package space.zhdanov.laboratory.reactive.chat

import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*

@SpringBootApplication
class ChatApplication {

    @Value("classpath:/static/index.html")
    lateinit var index: Resource

    @Bean fun routes(): RouterFunction<ServerResponse> {

        val router = RouterFunctions.resources("/**", ClassPathResource("static/"))

        return RouterFunctions.route(RequestPredicates.GET("/"),
                HandlerFunction {
                    ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .syncBody(index)
                }
        ).andRoute(RequestPredicates.POST("/login"),
                HandlerFunction { request ->

                    request.formData().flatMap { data ->

                        val login = data.getFirst("login") ?: ""
                        val password = DigestUtils.sha256Hex(data.getFirst("password")) ?: ""

                        if (login == "" || password == "")
                            return@flatMap ServerResponse.badRequest().build()
                        else {
                            val user = users.find { it.name == login }
                            if (user == null) {
                                val newUser = User(login, password)
                                users.add(newUser)
                                println(newUser)
                                return@flatMap ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                                        .body(Mono.just(newUser), User::class.java)
                            } else {
                                if (user.password == password) {
                                    println(user)
                                    return@flatMap ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                                            .body(Mono.just(user), User::class.java)
                                } else
                                    return@flatMap ServerResponse.status(HttpStatus.UNAUTHORIZED).build()
                            }
                        }
                    }
                }
        )
    }
}

val users = mutableListOf<User>()

fun main(args: Array<String>) {
    runApplication<ChatApplication>(*args)
}
