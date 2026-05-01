package com.toolbox.routes

import com.toolbox.repository.UserRepository
import com.toolbox.utils.JwtConfig
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.UUID

fun Route.uploadRoutes() {
    route("/api/upload") {

        // Authenticated user uploads their own avatar
        post("/avatar") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val jwt = token?.let { JwtConfig.verifyToken(it) }
            if (jwt == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "未认证"))
                return@post
            }
            val userId = jwt.subject.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "无效token"))
                return@post
            }

            val multipart = call.receiveMultipart()
            var fileName = ""
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val ext = part.originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
                    fileName = "avatar_${userId}_${UUID.randomUUID().toString().take(8)}.$ext"
                    val file = File("static/uploads/$fileName")
                    file.parentFile.mkdirs()
                    part.streamProvider().use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                }
                part.dispose()
            }

            if (fileName.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "未收到文件"))
                return@post
            }

            val url = "/uploads/$fileName"
            UserRepository.updateAvatar(userId, url)
            call.respond(mapOf("url" to url))
        }

        // Admin uploads notice image
        post("/notice-image") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@post
            }

            val multipart = call.receiveMultipart()
            var fileName = ""
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val ext = part.originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
                    fileName = "notice_${UUID.randomUUID().toString().take(8)}.$ext"
                    val file = File("static/uploads/$fileName")
                    file.parentFile.mkdirs()
                    part.streamProvider().use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                }
                part.dispose()
            }

            if (fileName.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "未收到文件"))
                return@post
            }

            val url = "/uploads/$fileName"
            call.respond(mapOf("url" to url))
        }
    }

    route("/api/admin") {

        // Admin changes any user's avatar
        post("/users/{id}/avatar") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@post
            }
            val userId = call.parameters["id"]?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的用户ID"))
                return@post
            }

            val multipart = call.receiveMultipart()
            var fileName = ""
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val ext = part.originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
                    fileName = "avatar_${userId}_${UUID.randomUUID().toString().take(8)}.$ext"
                    val file = File("static/uploads/$fileName")
                    file.parentFile.mkdirs()
                    part.streamProvider().use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                }
                part.dispose()
            }

            if (fileName.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "未收到文件"))
                return@post
            }

            val url = "/uploads/$fileName"
            UserRepository.updateAvatar(userId, url)
            call.respond(mapOf("url" to url))
        }

        // Admin updates user info
        put("/users/{id}/info") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@put
            }
            val userId = call.parameters["id"]?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的用户ID"))
                return@put
            }

            val body = call.receive<Map<String, String>>()
            val role = body["role"]
            if (role != null && (role == "user" || role == "admin")) {
                UserRepository.updateRole(userId, role)
            }
            call.respond(mapOf("status" to "ok"))
        }
    }
}
