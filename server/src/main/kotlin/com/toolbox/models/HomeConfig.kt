package com.toolbox.models

import kotlinx.serialization.Serializable

@Serializable
data class HomeConfig(
    val banners: List<BannerItem> = emptyList(),
    val quickTools: List<QuickToolItem> = emptyList(),
    val featuredCards: List<FeaturedCardItem> = emptyList(),
    val notices: List<NoticeItem> = emptyList()
)

@Serializable
data class BannerItem(
    val id: Long = 0,
    val title: String,
    val content: String,
    val sortOrder: Int = 0
)

@Serializable
data class QuickToolItem(
    val id: Long = 0,
    val toolId: String,
    val toolName: String,
    val iconName: String,
    val sortOrder: Int = 0,
    val enabled: Boolean = true
)

@Serializable
data class FeaturedCardItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val actionType: String = "",    // "tool", "community", "none"
    val actionValue: String = "",    // activity class name or nav target
    val sortOrder: Int = 0
)

@Serializable
data class NoticeItem(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: String = ""
)

// Request DTOs for admin operations
@Serializable
data class BannerSaveRequest(val banners: List<BannerItem>)

@Serializable
data class QuickToolSaveRequest(val tools: List<QuickToolItem>)

@Serializable
data class FeaturedCardSaveRequest(val cards: List<FeaturedCardItem>)

@Serializable
data class NoticeSaveRequest(val title: String, val content: String)
