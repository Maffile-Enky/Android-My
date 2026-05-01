package com.toolbox.repository

import com.toolbox.models.VersionInfo
import com.toolbox.models.VersionSaveRequest
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object VersionTable : LongIdTable("app_versions") {
    val versionCode = integer("version_code")
    val versionName = varchar("version_name", 50)
    val changelog = text("changelog")
    val apkUrl = varchar("apk_url", 500)
    val forceUpdate = bool("force_update").default(false)
    val createdAt = varchar("created_at", 20)
}

object VersionRepository {

    fun initTable() {
        transaction {
            SchemaUtils.create(VersionTable)
            seedDefaultVersion()
        }
    }

    private fun seedDefaultVersion() {
        if (VersionTable.selectAll().empty()) {
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            VersionTable.insert {
                it[versionCode] = 1
                it[versionName] = "1.0.0"
                it[changelog] = "初始版本"
                it[apkUrl] = ""
                it[forceUpdate] = false
                it[createdAt] = now
            }
        }
    }

    fun getLatest(): VersionInfo? = transaction {
        VersionTable.selectAll()
            .orderBy(VersionTable.versionCode to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.toVersionInfo()
    }

    fun save(request: VersionSaveRequest): VersionInfo = transaction {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val id = VersionTable.insertAndGetId {
            it[versionCode] = request.versionCode
            it[versionName] = request.versionName
            it[changelog] = request.changelog
            it[apkUrl] = request.apkUrl
            it[forceUpdate] = request.forceUpdate
            it[createdAt] = now
        }
        VersionInfo(id.value, request.versionCode, request.versionName, request.changelog, request.apkUrl, request.forceUpdate, now)
    }

    fun getAll(): List<VersionInfo> = transaction {
        VersionTable.selectAll()
            .orderBy(VersionTable.versionCode to SortOrder.DESC)
            .map { it.toVersionInfo() }
    }

    fun delete(id: Long): Boolean = transaction {
        VersionTable.deleteWhere { VersionTable.id eq id } > 0
    }

    private fun ResultRow.toVersionInfo() = VersionInfo(
        id = this[VersionTable.id].value,
        versionCode = this[VersionTable.versionCode],
        versionName = this[VersionTable.versionName],
        changelog = this[VersionTable.changelog],
        apkUrl = this[VersionTable.apkUrl],
        forceUpdate = this[VersionTable.forceUpdate],
        createdAt = this[VersionTable.createdAt]
    )
}
