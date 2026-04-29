package com.toolbox.services

import com.toolbox.models.Post
import com.toolbox.models.PageResponse
import com.toolbox.models.PostRequest
import com.toolbox.models.WsEvent
import com.toolbox.repository.PostRepository
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class PostService {

    private val sessions = ConcurrentHashMap.newKeySet<WebSocketSession>()

    fun getPosts(page: Int, size: Int): PageResponse {
        val (posts, total) = PostRepository.findAll(page, size)
        return PageResponse(posts, page, size, total)
    }

    fun getPost(id: Long): Post? = PostRepository.findById(id)

    fun createPost(request: PostRequest): Post {
        val post = PostRepository.create(request)
        broadcast(WsEvent(type = "new_post", post = post))
        return post
    }

    fun likePost(id: Long): Post? {
        val post = PostRepository.toggleLike(id) ?: return null
        broadcast(WsEvent(type = "post_liked", postId = post.id, likeCount = post.likes))
        return post
    }

    fun deletePost(id: Long, author: String): Boolean {
        val deleted = PostRepository.delete(id, author)
        if (deleted) {
            broadcast(WsEvent(type = "post_deleted", postId = id))
        }
        return deleted
    }

    fun registerSession(session: WebSocketSession) {
        sessions.add(session)
    }

    fun unregisterSession(session: WebSocketSession) {
        sessions.remove(session)
    }

    private fun broadcast(event: WsEvent) {
        val text = Json.encodeToString(event)
        sessions.forEach { session ->
            try {
                kotlinx.coroutines.runBlocking {
                    session.send(Frame.Text(text))
                }
            } catch (_: Exception) {
                sessions.remove(session)
            }
        }
    }
}
