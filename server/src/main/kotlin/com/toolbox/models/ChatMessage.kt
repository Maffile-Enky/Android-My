package com.toolbox.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: Long = 0,
    val fromUser: String,
    val toUser: String,
    val content: String,
    val createdAt: String = ""
)

@Serializable
data class ChatRequest(
    val fromUser: String,
    val toUser: String,
    val content: String
)

@Serializable
data class ChatListResponse(
    val messages: List<ChatMessage>
)
