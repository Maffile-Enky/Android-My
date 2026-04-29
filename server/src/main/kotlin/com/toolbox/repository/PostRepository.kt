package com.toolbox.repository

import com.toolbox.models.Post
import com.toolbox.models.PostRequest
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PostsTable : LongIdTable("posts") {
    val author = varchar("author", 50)
    val title = varchar("title", 200)
    val content = text("content")
    val likes = integer("likes").default(0)
    val createdAt = varchar("created_at", 20)
}

object PostRepository {

    fun initTable() {
        transaction {
            SchemaUtils.create(PostsTable)
        }
    }

    fun findAll(page: Int, size: Int): Pair<List<Post>, Long> = transaction {
        val total = PostsTable.selectAll().count()
        val rows = PostsTable.selectAll()
            .orderBy(PostsTable.id to SortOrder.DESC)
            .limit(size)
            .offset((page * size).toLong())
            .toList()
        val posts = rows.map { it.toPost() }
        Pair(posts, total)
    }

    fun findById(id: Long): Post? = transaction {
        PostsTable.selectAll().where { PostsTable.id eq id }
            .singleOrNull()?.toPost()
    }

    fun create(request: PostRequest): Post = transaction {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val id = PostsTable.insertAndGetId {
            it[author] = request.author
            it[title] = request.title
            it[content] = request.content
            it[likes] = 0
            it[createdAt] = now
        }
        Post(id.value, request.author, request.title, request.content, 0, now)
    }

    fun toggleLike(id: Long): Post? = transaction {
        val post = PostsTable.selectAll().where { PostsTable.id eq id }.singleOrNull() ?: return@transaction null
        val newLikes = post[PostsTable.likes] + 1
        PostsTable.update({ PostsTable.id eq id }) {
            it[likes] = newLikes
        }
        post.toPost().copy(likes = newLikes)
    }

    fun delete(id: Long, author: String): Boolean = transaction {
        PostsTable.deleteWhere { (PostsTable.id eq id) and (PostsTable.author eq author) } > 0
    }

    private fun ResultRow.toPost(): Post = Post(
        id = this[PostsTable.id].value,
        author = this[PostsTable.author],
        title = this[PostsTable.title],
        content = this[PostsTable.content],
        likes = this[PostsTable.likes],
        createdAt = this[PostsTable.createdAt]
    )
}
