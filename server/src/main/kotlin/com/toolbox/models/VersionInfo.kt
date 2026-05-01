package com.toolbox.models

import kotlinx.serialization.Serializable

@Serializable
data class VersionInfo(
    val id: Long = 0,
    val versionCode: Int,
    val versionName: String,
    val changelog: String,
    val apkUrl: String,
    val forceUpdate: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class VersionCheckResponse(
    val hasUpdate: Boolean,
    val latestVersion: VersionInfo? = null
)

@Serializable
data class VersionSaveRequest(
    val versionCode: Int,
    val versionName: String,
    val changelog: String,
    val apkUrl: String,
    val forceUpdate: Boolean = false
)
