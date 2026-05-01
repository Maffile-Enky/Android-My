package com.toolbox

import com.toolbox.plugins.*
import com.toolbox.routes.*
import com.toolbox.services.AuthService
import com.toolbox.services.ChatService
import com.toolbox.services.HomeService
import com.toolbox.services.PostService
import com.toolbox.services.VersionService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import java.io.File

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
    val authService = AuthService()
    val versionService = VersionService()

    routing {
        // Static file serving
        staticFiles("/admin", File("static/admin"))
        staticFiles("/files", File("static/files"))
        staticFiles("/uploads", File("static/uploads"))

        // Public routes
        authRoutes(authService)
        uploadRoutes()
        postRoutes(postService)
        webSocketRoutes(postService)
        chatRoutes(chatService)
        homeRoutes(homeService)
        versionRoutes(versionService)

        // Admin routes
        adminRoutes(authService)
    }
}
