package com.toolbox.routes

import com.toolbox.models.VersionSaveRequest
import com.toolbox.services.VersionService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.versionRoutes(versionService: VersionService) {
    route("/api/version") {

        get("/check") {
            val versionCode = call.request.queryParameters["versionCode"]?.toIntOrNull()
            if (versionCode == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "缺少 versionCode 参数"))
                return@get
            }
            call.respond(versionService.checkForUpdate(versionCode))
        }

        get("/latest") {
            val latest = versionService.getLatestVersion()
            if (latest != null) {
                call.respond(latest)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "暂无版本信息"))
            }
        }
    }

    route("/api/admin/versions") {

        get("/") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@get
            }
            call.respond(mapOf("versions" to versionService.getAllVersions()))
        }

        post("/") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@post
            }
            val request = call.receive<VersionSaveRequest>()
            val version = versionService.saveVersion(request)
            call.respond(HttpStatusCode.Created, version)
        }

        delete("/{id}") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@delete
            }
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "无效的版本ID"))
                return@delete
            }
            if (versionService.deleteVersion(id)) {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "版本不存在"))
            }
        }
    }
}
