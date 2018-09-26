package space.zhdanov.laboratory.reactive.chat

data class Message(val user: User, val data: Any, val type: TypeMessage)

enum class TypeMessage {
    TEXT,
    USERS
}

data class User(val name: String, val password: String?, var online: Boolean = false)