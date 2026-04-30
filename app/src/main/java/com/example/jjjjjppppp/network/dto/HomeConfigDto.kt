package com.example.jjjjjppppp.network.dto

import com.google.gson.annotations.SerializedName

data class HomeConfigDto(
    @SerializedName("banners") val banners: List<BannerItemDto> = emptyList(),
    @SerializedName("quickTools") val quickTools: List<QuickToolItemDto> = emptyList(),
    @SerializedName("featuredCards") val featuredCards: List<FeaturedCardItemDto> = emptyList(),
    @SerializedName("notices") val notices: List<NoticeItemDto> = emptyList()
)

data class BannerItemDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("sortOrder") val sortOrder: Int = 0
)

data class QuickToolItemDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("toolId") val toolId: String = "",
    @SerializedName("toolName") val toolName: String = "",
    @SerializedName("iconName") val iconName: String = "",
    @SerializedName("sortOrder") val sortOrder: Int = 0,
    @SerializedName("enabled") val enabled: Boolean = true
)

data class FeaturedCardItemDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("actionType") val actionType: String = "",
    @SerializedName("actionValue") val actionValue: String = "",
    @SerializedName("sortOrder") val sortOrder: Int = 0
)

data class NoticeItemDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("createdAt") val createdAt: String = ""
)
