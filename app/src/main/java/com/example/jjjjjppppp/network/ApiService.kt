package com.example.jjjjjppppp.network

import com.example.jjjjjppppp.network.dto.ChatListResponseDto
import com.example.jjjjjppppp.network.dto.ConversationListResponseDto
import com.example.jjjjjppppp.network.dto.ChatMessageDto
import com.example.jjjjjppppp.network.dto.ChatRequestDto
import com.example.jjjjjppppp.network.dto.PageResponseDto
import com.example.jjjjjppppp.network.dto.PostDto
import com.example.jjjjjppppp.network.dto.PostRequestDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("api/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponseDto>

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") id: Long): Response<PostDto>

    @POST("api/posts")
    suspend fun createPost(@Body request: PostRequestDto): Response<PostDto>

    @POST("api/posts/{id}/like")
    suspend fun likePost(@Path("id") id: Long): Response<PostDto>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(
        @Path("id") id: Long,
        @Query("author") author: String
    ): Response<Unit>

    @GET("api/chat/messages")
    suspend fun getChatMessages(
        @Query("user") user: String,
        @Query("target") target: String
    ): Response<ChatListResponseDto>

    @POST("api/chat/send")
    suspend fun sendChatMessage(@Body request: ChatRequestDto): Response<ChatMessageDto>

    @GET("api/chat/conversations")
    suspend fun getConversations(@Query("user") user: String): Response<ConversationListResponseDto>
}
