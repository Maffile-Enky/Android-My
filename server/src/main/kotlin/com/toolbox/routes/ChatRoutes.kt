package com.toolbox.routes

import com.toolbox.models.ChatRequest
import com.toolbox.models.ChatListResponse
import com.toolbox.models.ConversationListResponse
import com.toolbox.services.ChatService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.chatRoutes(chatService: ChatService) {

    route("/api/chat") {

        get("/conversations") {
            val user = call.request.queryParameters["user"] ?: ""
            if (user.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "user required")
                return@get
            }
            val conversations = chatService.getConversations(user)
            call.respond(ConversationListResponse(conversations))
        }

        get("/messages") {
            val user = call.request.queryParameters["user"] ?: ""
            val target = call.request.queryParameters["target"] ?: ""
            if (user.isBlank() || target.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "user and target required")
                return@get
            }
            val messages = chatService.getMessages(user, target)
            call.respond(ChatListResponse(messages))
        }

        post("/send") {
            val req = call.receive<ChatRequest>()
            if (req.fromUser.isBlank() || req.toUser.isBlank() || req.content.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "All fields required")
                return@post
            }
            val msg = chatService.sendMessage(req.fromUser, req.toUser, req.content)
            call.respond(HttpStatusCode.Created, msg)
        }
    }

    webSocket("/ws/chat") {
        val username = call.request.queryParameters["user"] ?: ""
        if (username.isBlank()) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing user parameter"))
            return@webSocket
        }

        chatService.registerUserSession(username, this)

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        try {
                            val req = kotlinx.serialization.json.Json.decodeFromString<ChatRequest>(text)
                            if (req.fromUser.isNotBlank() && req.toUser.isNotBlank() && req.content.isNotBlank()) {
                                chatService.sendMessage(req.fromUser, req.toUser, req.content)
                            }
                        } catch (_: Exception) {}
                    }
                    else -> {}
                }
            }
        } finally {
            chatService.unregisterUserSession(username)
        }
    }
}
