package com.example.jjjjjppppp.network.dto

import com.google.gson.annotations.SerializedName

data class VersionInfoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("versionCode") val versionCode: Int,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("changelog") val changelog: String,
    @SerializedName("apkUrl") val apkUrl: String,
    @SerializedName("forceUpdate") val forceUpdate: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class VersionCheckResponseDto(
    @SerializedName("hasUpdate") val hasUpdate: Boolean,
    @SerializedName("latestVersion") val latestVersion: VersionInfoDto?
)
