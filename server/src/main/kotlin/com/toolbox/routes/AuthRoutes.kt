package com.toolbox.routes

import com.toolbox.models.LoginRequest
import com.toolbox.models.RegisterRequest
import com.toolbox.services.AuthService
import com.toolbox.utils.JwtConfig
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()
            try {
                val response = authService.register(request.username, request.password)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            try {
                val response = authService.login(request.username, request.password)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
            }
        }

        get("/me") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "未提供认证令牌"))
                return@get
            }
            val jwt = JwtConfig.verifyToken(token)
            if (jwt == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "令牌无效或已过期"))
                return@get
            }
            val userId = jwt.subject.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "令牌无效"))
                return@get
            }
            val user = authService.getProfile(userId)
            if (user != null) {
                call.respond(user)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "用户不存在"))
            }
        }
    }
}
