package com.toolbox.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = 0,
    val username: String,
    val role: String,
    val avatar: String = "",
    val createdAt: String = ""
)

@Serializable
data class RegisterRequest(val username: String, val password: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: User)

@Serializable
data class UserListResponse(val users: List<User>)

@Serializable
data class RoleUpdateRequest(val role: String)
