package com.toolbox

import com.toolbox.plugins.*
import com.toolbox.routes.PostRoutes
import com.toolbox.routes.WebSocketRoutes
import com.toolbox.services.PostService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureWebSockets()
    configureCORS()
    configureDatabase()

    val postService = PostService()

    PostRoutes(this, postService)
    WebSocketRoutes(this, postService)
}
