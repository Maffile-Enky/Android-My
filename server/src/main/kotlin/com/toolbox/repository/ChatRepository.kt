package com.toolbox.repository

import com.toolbox.models.ChatMessage
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ChatMessagesTable : LongIdTable("chat_messages") {
    val fromUser = varchar("from_user", 50)
    val toUser = varchar("to_user", 50)
    val content = text("content")
    val createdAt = varchar("created_at", 20)
}

object ChatRepository {

    fun initTable() {
        transaction {
            SchemaUtils.create(ChatMessagesTable)
        }
    }

    fun getMessages(user1: String, user2: String): List<ChatMessage> = transaction {
        ChatMessagesTable.selectAll()
            .where {
                ((ChatMessagesTable.fromUser eq user1) and (ChatMessagesTable.toUser eq user2)) or
                ((ChatMessagesTable.fromUser eq user2) and (ChatMessagesTable.toUser eq user1))
            }
            .orderBy(ChatMessagesTable.id to SortOrder.ASC)
            .limit(200)
            .toList()
            .map { it.toChatMessage() }
    }

    fun save(fromUser: String, toUser: String, content: String): ChatMessage = transaction {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val id = ChatMessagesTable.insertAndGetId {
            it[ChatMessagesTable.fromUser] = fromUser
            it[ChatMessagesTable.toUser] = toUser
            it[ChatMessagesTable.content] = content
            it[createdAt] = now
        }
        ChatMessage(id.value, fromUser, toUser, content, now)
    }

    private fun ResultRow.toChatMessage(): ChatMessage = ChatMessage(
        id = this[ChatMessagesTable.id].value,
        fromUser = this[ChatMessagesTable.fromUser],
        toUser = this[ChatMessagesTable.toUser],
        content = this[ChatMessagesTable.content],
        createdAt = this[ChatMessagesTable.createdAt]
    )
}
