package space.zhdanov.laboratory.reactive.chat

import com.fasterxml.jackson.annotation.JsonIgnore

data class Message(val user: User, val data: Any, val type: TypeMessage)

enum class TypeMessage {
    TEXT,
    USERS
}

data class User(val name: String, @JsonIgnore val password: String?)