package com.example.jjjjjppppp.repository

import com.example.jjjjjppppp.network.RetrofitClient
import com.example.jjjjjppppp.network.dto.ChatMessageDto
import com.example.jjjjjppppp.network.dto.ChatRequestDto

class ChatRepository {

    suspend fun getMessages(user: String, target: String): Result<List<ChatMessageDto>> {
        return try {
            val response = RetrofitClient.apiService.getChatMessages(user, target)
            if (response.isSuccessful) {
                Result.success(response.body()?.messages ?: emptyList())
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(fromUser: String, toUser: String, content: String): Result<ChatMessageDto> {
        return try {
            val response = RetrofitClient.apiService.sendChatMessage(
                ChatRequestDto(fromUser, toUser, content)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
