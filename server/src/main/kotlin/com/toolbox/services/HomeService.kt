package com.toolbox.services

import com.toolbox.models.*
import com.toolbox.repository.HomeRepository

class HomeService {

    fun getFullConfig(): HomeConfig = HomeRepository.getFullConfig()

    // Banners
    fun getBanners(): List<BannerItem> = HomeRepository.getAllBanners()
    fun saveBanners(request: BannerSaveRequest) { HomeRepository.saveBanners(request.banners) }
    fun deleteBanner(id: Long): Boolean = HomeRepository.deleteBanner(id)

    // Quick Tools
    fun getTools(): List<QuickToolItem> = HomeRepository.getAllTools()
    fun saveAllTools(request: QuickToolSaveRequest) { HomeRepository.saveAllTools(request.tools) }

    // Featured Cards
    fun getFeatured(): List<FeaturedCardItem> = HomeRepository.getAllFeatured()
    fun saveAllFeatured(request: FeaturedCardSaveRequest) { HomeRepository.saveAllFeatured(request.cards) }

    // Notices
    fun getNotices(): List<NoticeItem> = HomeRepository.getAllNotices()
    fun addNotice(request: NoticeSaveRequest): NoticeItem = HomeRepository.addNotice(request.title, request.content)
    fun deleteNotice(id: Long): Boolean = HomeRepository.deleteNotice(id)
}
