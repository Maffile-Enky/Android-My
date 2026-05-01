package com.example.jjjjjppppp.network.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String,
    @SerializedName("avatar") val avatar: String = "",
    @SerializedName("createdAt") val createdAt: String
)

data class RegisterRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

data class UploadResponseDto(
    @SerializedName("url") val url: String
)
