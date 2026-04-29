package com.toolbox.routes

import com.toolbox.services.PostService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun WebSocketRoutes(app: Application, postService: PostService) {
    app.routing {
        webSocket("/ws") {
            postService.registerSession(this)
            try {
                for (frame in incoming) {
                    // Heartbeat / keep-alive; no client-to-server messages needed for now
                    if (frame is Frame.Text) {
                        // Can handle client messages here if needed later
                    }
                }
            } finally {
                postService.unregisterSession(this)
            }
        }
    }
}
