package com.example.jjjjjppppp.network.dto

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("fromUser") val fromUser: String,
    @SerializedName("toUser") val toUser: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String
)

data class ChatRequestDto(
    @SerializedName("fromUser") val fromUser: String,
    @SerializedName("toUser") val toUser: String,
    @SerializedName("content") val content: String
)

data class ChatListResponseDto(
    @SerializedName("messages") val messages: List<ChatMessageDto>
)

data class ConversationDto(
    @SerializedName("targetUser") val targetUser: String,
    @SerializedName("lastMessage") val lastMessage: String,
    @SerializedName("lastTime") val lastTime: String,
    @SerializedName("isFromMe") val isFromMe: Boolean
)

data class ConversationListResponseDto(
    @SerializedName("conversations") val conversations: List<ConversationDto>
)
