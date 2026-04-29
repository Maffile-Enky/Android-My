package com.toolbox.models

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Long = 0,
    val author: String,
    val title: String,
    val content: String,
    val likes: Int = 0,
    val createdAt: String = ""
)
