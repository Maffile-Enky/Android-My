package com.toolbox.repository

import com.toolbox.models.User
import at.favre.lib.crypto.bcrypt.BCrypt
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object UsersTable : LongIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val role = varchar("role", 20).default("user")
    val avatar = varchar("avatar", 500).default("")
    val createdAt = varchar("created_at", 20)
}

object UserRepository {

    fun initTable() {
        transaction {
            SchemaUtils.create(UsersTable)
            // Migration: add avatar column if missing
            try {
                exec("ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar VARCHAR(500) DEFAULT ''")
            } catch (_: Exception) {}
            seedAdminUser()
        }
    }

    private fun seedAdminUser() {
        if (UsersTable.selectAll().where { UsersTable.username eq "root" }.empty()) {
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val hash = BCrypt.withDefaults().hashToString(12, "root".toCharArray())
            UsersTable.insert {
                it[username] = "root"
                it[passwordHash] = hash
                it[role] = "admin"
                it[createdAt] = now
            }
        }
    }

    fun findByUsername(username: String): Pair<Long, String>? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.username eq username }
            .firstOrNull()
            ?.let { it[UsersTable.id].value to it[UsersTable.passwordHash] }
    }

    fun findById(id: Long): User? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .firstOrNull()
            ?.toUser()
    }

    fun create(username: String, passwordHash: String, role: String = "user"): User = transaction {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val id = UsersTable.insertAndGetId {
            it[UsersTable.username] = username
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.role] = role
            it[createdAt] = now
        }
        User(id.value, username, role, now)
    }

    fun findAll(): List<User> = transaction {
        UsersTable.selectAll()
            .orderBy(UsersTable.id to SortOrder.DESC)
            .map { it.toUser() }
    }

    fun delete(id: Long): Boolean = transaction {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }

    fun updateRole(id: Long, role: String): Boolean = transaction {
        UsersTable.update({ UsersTable.id eq id }) {
            it[UsersTable.role] = role
        } > 0
    }

    fun updateAvatar(id: Long, avatarUrl: String): Boolean = transaction {
        UsersTable.update({ UsersTable.id eq id }) {
            it[avatar] = avatarUrl
        } > 0
    }

    fun exists(username: String): Boolean = transaction {
        !UsersTable.selectAll().where { UsersTable.username eq username }.empty()
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id].value,
        username = this[UsersTable.username],
        role = this[UsersTable.role],
        avatar = this[UsersTable.avatar],
        createdAt = this[UsersTable.createdAt]
    )
}
