package com.toolbox.routes

import com.toolbox.models.RoleUpdateRequest
import com.toolbox.repository.PostRepository
import com.toolbox.services.AuthService
import com.toolbox.utils.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun ApplicationCall.requireAdmin(): Boolean {
    val token = request.headers["Authorization"]?.removePrefix("Bearer ") ?: return false
    val jwt = JwtConfig.verifyToken(token) ?: return false
    return jwt.getClaim("role").asString() == "admin"
}

fun Route.adminRoutes(authService: AuthService) {
    route("/api/admin") {

        // Admin can delete any post without author check
        delete("/posts/{id}") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@delete
            }
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的帖子ID"))
                return@delete
            }
            val deleted = com.toolbox.repository.PostRepository.adminDelete(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "帖子不存在"))
            }
        }

        get("/users") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@get
            }
            call.respond(mapOf("users" to authService.getAllUsers()))
        }

        delete("/users/{id}") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@delete
            }
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的用户ID"))
                return@delete
            }
            try {
                if (authService.deleteUser(id)) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "用户不存在"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        put("/users/{id}/role") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@put
            }
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的用户ID"))
                return@put
            }
            val request = call.receive<RoleUpdateRequest>()
            try {
                if (authService.updateUserRole(id, request.role)) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "用户不存在"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}
