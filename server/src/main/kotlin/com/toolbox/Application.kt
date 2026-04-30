package com.toolbox

import com.toolbox.plugins.*
import com.toolbox.routes.*
import com.toolbox.services.ChatService
import com.toolbox.services.HomeService
import com.toolbox.services.PostService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureWebSockets()
    configureCORS()
    configureDatabase()

    val postService = PostService()
    val chatService = ChatService()
    val homeService = HomeService()

    routing {
        postRoutes(postService)
        webSocketRoutes(postService)
        chatRoutes(chatService)
        homeRoutes(homeService)
    }
}
