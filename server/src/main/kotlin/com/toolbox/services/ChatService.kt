package com.toolbox.services

import com.toolbox.models.ChatMessage
import com.toolbox.repository.ChatRepository
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class ChatService {

    // username -> WebSocket session
    private val userSessions = ConcurrentHashMap<String, WebSocketSession>()

    fun getMessages(user: String, target: String): List<ChatMessage> {
        return ChatRepository.getMessages(user, target)
    }

    fun getConversations(user: String): List<com.toolbox.models.Conversation> {
        return ChatRepository.getConversations(user)
    }

    fun sendMessage(fromUser: String, toUser: String, content: String): ChatMessage {
        val msg = ChatRepository.save(fromUser, toUser, content)
        val json = Json.encodeToString(msg)

        // Push to recipient if online
        userSessions[toUser]?.let { session ->
            try {
                kotlinx.coroutines.runBlocking {
                    session.send(Frame.Text(json))
                }
            } catch (_: Exception) {
                userSessions.remove(toUser)
            }
        }

        // Also push to sender (for multi-device sync)
        userSessions[fromUser]?.let { session ->
            try {
                kotlinx.coroutines.runBlocking {
                    session.send(Frame.Text(json))
                }
            } catch (_: Exception) {
                userSessions.remove(fromUser)
            }
        }

        return msg
    }

    fun registerUserSession(username: String, session: WebSocketSession) {
        userSessions[username] = session
    }

    fun unregisterUserSession(username: String) {
        userSessions.remove(username)
    }
}
