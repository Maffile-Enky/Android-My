package com.example.jjjjjppppp.network

import android.util.Log
import com.example.jjjjjppppp.network.dto.WsEventDto
import com.google.gson.Gson
import okhttp3.*

class WebSocketManager {

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    var onNewPost: ((postId: Long) -> Unit)? = null
    var onPostLiked: ((postId: Long, likeCount: Int) -> Unit)? = null
    var onPostDeleted: ((postId: Long) -> Unit)? = null
    var onConnectionState: ((Boolean) -> Unit)? = null

    fun connect(serverBaseUrl: String) {
        val wsUrl = serverBaseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/') + "/ws"

        val request = Request.Builder().url(wsUrl).build()
        webSocket = RetrofitClient.okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
                onConnectionState?.invoke(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val event = gson.fromJson(text, WsEventDto::class.java)
                    when (event.type) {
                        "new_post" -> {
                            event.post?.id?.let { onNewPost?.invoke(it) }
                        }
                        "post_liked" -> {
                            event.postId?.let { id ->
                                onPostLiked?.invoke(id, event.likeCount ?: 0)
                            }
                        }
                        "post_deleted" -> {
                            event.postId?.let { onPostDeleted?.invoke(it) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Failed to parse message: $text", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection failed", t)
                onConnectionState?.invoke(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closed: $reason")
                onConnectionState?.invoke(false)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Fragment paused")
        webSocket = null
    }
}
