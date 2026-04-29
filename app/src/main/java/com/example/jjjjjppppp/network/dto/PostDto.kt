package com.example.jjjjjppppp.network.dto

import com.google.gson.annotations.SerializedName

data class PostDto(
    @SerializedName("id") val id: Long,
    @SerializedName("author") val author: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("likes") val likes: Int,
    @SerializedName("createdAt") val createdAt: String
)

data class PostRequestDto(
    @SerializedName("author") val author: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String
)

data class PageResponseDto(
    @SerializedName("posts") val posts: List<PostDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total") val total: Long
)

data class WsEventDto(
    @SerializedName("type") val type: String,
    @SerializedName("post") val post: PostDto?,
    @SerializedName("postId") val postId: Long?,
    @SerializedName("likeCount") val likeCount: Int?
)
