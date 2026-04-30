package com.toolbox.repository

import com.toolbox.models.*
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BannersTable : LongIdTable("home_banners") {
    val title = varchar("title", 200)
    val content = text("content")
    val sortOrder = integer("sort_order").default(0)
}

object QuickToolsTable : LongIdTable("home_tools") {
    val toolId = varchar("tool_id", 50)
    val toolName = varchar("tool_name", 100)
    val iconName = varchar("icon_name", 50)
    val sortOrder = integer("sort_order").default(0)
    val enabled = bool("enabled").default(true)
}

object FeaturedCardsTable : LongIdTable("home_featured") {
    val title = varchar("title", 200)
    val description = text("description")
    val actionType = varchar("action_type", 50).default("")
    val actionValue = varchar("action_value", 200).default("")
    val sortOrder = integer("sort_order").default(0)
}

object NoticesTable : LongIdTable("home_notices") {
    val title = varchar("title", 200)
    val content = text("content")
    val createdAt = varchar("created_at", 20)
}

object HomeRepository {

    fun initTable() {
        transaction {
            SchemaUtils.create(BannersTable, QuickToolsTable, FeaturedCardsTable, NoticesTable)
            seedDefaultData()
        }
    }

    private fun seedDefaultData() {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

        // Seed default banner
        if (BannersTable.selectAll().empty()) {
            BannersTable.insert {
                it[title] = "欢迎使用工具箱"
                it[content] = "实用工具，简单生活"
                it[sortOrder] = 0
            }
            BannersTable.insert {
                it[title] = "社区功能已上线"
                it[content] = "加入社区，分享你的使用心得"
                it[sortOrder] = 1
            }
        }

        // Seed default quick tools (4 tools)
        if (QuickToolsTable.selectAll().empty()) {
            val tools = listOf(
                Triple("counter", "计数器", "ic_counter"),
                Triple("bmi", "BMI计算", "ic_bmi"),
                Triple("stopwatch", "秒表", "ic_stopwatch"),
                Triple("notes", "便签", "ic_note")
            )
            tools.forEachIndexed { index, (id, name, icon) ->
                QuickToolsTable.insert {
                    it[toolId] = id
                    it[toolName] = name
                    it[iconName] = icon
                    it[sortOrder] = index
                    it[enabled] = true
                }
            }
        }

        // Seed default featured card
        if (FeaturedCardsTable.selectAll().empty()) {
            FeaturedCardsTable.insert {
                it[title] = "加入社区讨论"
                it[description] = "分享你的工具使用心得，与其他用户交流"
                it[actionType] = "community"
                it[actionValue] = ""
                it[sortOrder] = 0
            }
        }

        // Seed default notice
        if (NoticesTable.selectAll().empty()) {
            NoticesTable.insert {
                it[title] = "系统通知"
                it[content] = "工具箱 v0.3.0 已上线，全新主题切换功能等你体验"
                it[createdAt] = now
            }
        }
    }

    // ==================== Banners ====================

    fun getAllBanners(): List<BannerItem> = transaction {
        BannersTable.selectAll()
            .orderBy(BannersTable.sortOrder to SortOrder.ASC)
            .map { it.toBanner() }
    }

    fun saveBanners(banners: List<BannerItem>) = transaction {
        BannersTable.deleteAll()
        banners.forEachIndexed { index, b ->
            BannersTable.insert {
                it[title] = b.title
                it[content] = b.content
                it[sortOrder] = b.sortOrder.takeIf { it > 0 } ?: index
            }
        }
    }

    fun deleteBanner(id: Long): Boolean = transaction {
        BannersTable.deleteWhere { BannersTable.id eq id } > 0
    }

    // ==================== Quick Tools ====================

    fun getAllTools(): List<QuickToolItem> = transaction {
        QuickToolsTable.selectAll()
            .where { QuickToolsTable.enabled eq true }
            .orderBy(QuickToolsTable.sortOrder to SortOrder.ASC)
            .map { it.toTool() }
    }

    fun saveAllTools(tools: List<QuickToolItem>) = transaction {
        QuickToolsTable.deleteAll()
        tools.forEachIndexed { index, t ->
            QuickToolsTable.insert {
                it[toolId] = t.toolId
                it[toolName] = t.toolName
                it[iconName] = t.iconName
                it[sortOrder] = t.sortOrder.takeIf { it > 0 } ?: index
                it[enabled] = t.enabled
            }
        }
    }

    // ==================== Featured Cards ====================

    fun getAllFeatured(): List<FeaturedCardItem> = transaction {
        FeaturedCardsTable.selectAll()
            .orderBy(FeaturedCardsTable.sortOrder to SortOrder.ASC)
            .map { it.toFeatured() }
    }

    fun saveAllFeatured(cards: List<FeaturedCardItem>) = transaction {
        FeaturedCardsTable.deleteAll()
        cards.forEachIndexed { index, c ->
            FeaturedCardsTable.insert {
                it[title] = c.title
                it[description] = c.description
                it[actionType] = c.actionType
                it[actionValue] = c.actionValue
                it[sortOrder] = c.sortOrder.takeIf { it > 0 } ?: index
            }
        }
    }

    // ==================== Notices ====================

    fun getAllNotices(): List<NoticeItem> = transaction {
        NoticesTable.selectAll()
            .orderBy(NoticesTable.id to SortOrder.DESC)
            .limit(20)
            .map { it.toNotice() }
    }

    fun addNotice(title: String, content: String): NoticeItem = transaction {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val id = NoticesTable.insertAndGetId {
            it[NoticesTable.title] = title
            it[NoticesTable.content] = content
            it[createdAt] = now
        }
        NoticeItem(id.value, title, content, now)
    }

    fun deleteNotice(id: Long): Boolean = transaction {
        NoticesTable.deleteWhere { NoticesTable.id eq id } > 0
    }

    // ==================== Full Config ====================

    fun getFullConfig(): HomeConfig = transaction {
        HomeConfig(
            banners = BannersTable.selectAll()
                .orderBy(BannersTable.sortOrder to SortOrder.ASC)
                .map { it.toBanner() },
            quickTools = QuickToolsTable.selectAll()
                .where { QuickToolsTable.enabled eq true }
                .orderBy(QuickToolsTable.sortOrder to SortOrder.ASC)
                .map { it.toTool() },
            featuredCards = FeaturedCardsTable.selectAll()
                .orderBy(FeaturedCardsTable.sortOrder to SortOrder.ASC)
                .map { it.toFeatured() },
            notices = NoticesTable.selectAll()
                .orderBy(NoticesTable.id to SortOrder.DESC)
                .limit(20)
                .map { it.toNotice() }
        )
    }

    // ==================== Row Mappers ====================

    private fun ResultRow.toBanner() = BannerItem(
        id = this[BannersTable.id].value,
        title = this[BannersTable.title],
        content = this[BannersTable.content],
        sortOrder = this[BannersTable.sortOrder]
    )

    private fun ResultRow.toTool() = QuickToolItem(
        id = this[QuickToolsTable.id].value,
        toolId = this[QuickToolsTable.toolId],
        toolName = this[QuickToolsTable.toolName],
        iconName = this[QuickToolsTable.iconName],
        sortOrder = this[QuickToolsTable.sortOrder],
        enabled = this[QuickToolsTable.enabled]
    )

    private fun ResultRow.toFeatured() = FeaturedCardItem(
        id = this[FeaturedCardsTable.id].value,
        title = this[FeaturedCardsTable.title],
        description = this[FeaturedCardsTable.description],
        actionType = this[FeaturedCardsTable.actionType],
        actionValue = this[FeaturedCardsTable.actionValue],
        sortOrder = this[FeaturedCardsTable.sortOrder]
    )

    private fun ResultRow.toNotice() = NoticeItem(
        id = this[NoticesTable.id].value,
        title = this[NoticesTable.title],
        content = this[NoticesTable.content],
        createdAt = this[NoticesTable.createdAt]
    )
}
