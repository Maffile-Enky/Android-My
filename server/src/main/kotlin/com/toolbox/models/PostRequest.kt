package com.toolbox.models

import kotlinx.serialization.Serializable

@Serializable
data class PostRequest(
    val author: String,
    val title: String,
    val content: String
)

@Serializable
data class PageResponse(
    val posts: List<Post>,
    val page: Int,
    val size: Int,
    val total: Long
)

@Serializable
data class WsEvent(
    val type: String,
    val post: Post? = null,
    val postId: Long? = null,
    val likeCount: Int? = null
)
