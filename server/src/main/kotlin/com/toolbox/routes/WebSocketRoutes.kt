package com.toolbox.routes

import com.toolbox.services.PostService
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.webSocketRoutes(postService: PostService) {
    webSocket("/ws") {
        postService.registerSession(this)
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    // Heartbeat; no client-to-server messages needed
                }
            }
        } finally {
            postService.unregisterSession(this)
        }
    }
}
