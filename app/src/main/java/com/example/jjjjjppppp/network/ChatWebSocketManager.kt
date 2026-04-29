package com.example.jjjjjppppp.network

import android.util.Log
import com.example.jjjjjppppp.network.dto.ChatMessageDto
import com.google.gson.Gson
import okhttp3.*

class ChatWebSocketManager {

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    var onMessageReceived: ((ChatMessageDto) -> Unit)? = null
    var onConnectionState: ((Boolean) -> Unit)? = null

    fun connect(serverBaseUrl: String, username: String) {
        val wsUrl = serverBaseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/') + "/ws/chat?user=$username"

        val request = Request.Builder().url(wsUrl).build()
        webSocket = RetrofitClient.okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatWS", "Connected as $username")
                onConnectionState?.invoke(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val msg = gson.fromJson(text, ChatMessageDto::class.java)
                    onMessageReceived?.invoke(msg)
                } catch (e: Exception) {
                    Log.e("ChatWS", "Parse error: $text", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatWS", "Connection failed", t)
                onConnectionState?.invoke(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ChatWS", "Closed: $reason")
                onConnectionState?.invoke(false)
            }
        })
    }

    fun sendMessage(request: com.example.jjjjjppppp.network.dto.ChatRequestDto) {
        val json = gson.toJson(request)
        webSocket?.send(json)
    }

    fun disconnect() {
        webSocket?.close(1000, "Chat closed")
        webSocket = null
    }
}
