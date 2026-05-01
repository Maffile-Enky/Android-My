package com.toolbox.routes

import com.toolbox.models.*
import com.toolbox.services.HomeService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.homeRoutes(homeService: HomeService) {
    route("/api/home") {

        // Get full home page config
        get("/config") {
            call.respond(homeService.getFullConfig())
        }

        // ==================== Banners ====================
        get("/banners") {
            call.respond(homeService.getBanners())
        }

        post("/banners") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@post
            }
            val request = call.receive<BannerSaveRequest>()
            homeService.saveBanners(request)
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }

        delete("/banners/{id}") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@delete
            }
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null || !homeService.deleteBanner(id)) {
                call.respond(HttpStatusCode.NotFound, "Banner not found")
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            }
        }

        // ==================== Quick Tools ====================
        get("/tools") {
            call.respond(homeService.getTools())
        }

        post("/tools") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@post
            }
            val request = call.receive<QuickToolSaveRequest>()
            homeService.saveAllTools(request)
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }

        // ==================== Featured Cards ====================
        get("/featured") {
            call.respond(homeService.getFeatured())
        }

        post("/featured") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@post
            }
            val request = call.receive<FeaturedCardSaveRequest>()
            homeService.saveAllFeatured(request)
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }

        // ==================== Notices ====================
        get("/notices") {
            call.respond(homeService.getNotices())
        }

        post("/notices") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@post
            }
            val request = call.receive<NoticeSaveRequest>()
            if (request.title.isBlank() || request.content.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Title and content are required")
                return@post
            }
            val notice = homeService.addNotice(request)
            call.respond(HttpStatusCode.Created, notice)
        }

        delete("/notices/{id}") {
            if (!call.requireAdmin()) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "需要管理员权限"))
                return@delete
            }
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null || !homeService.deleteNotice(id)) {
                call.respond(HttpStatusCode.NotFound, "Notice not found")
            } else {
                call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
            }
        }
    }
}
