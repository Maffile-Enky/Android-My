package com.toolbox.routes

import com.toolbox.models.PostRequest
import com.toolbox.services.PostService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postRoutes(postService: PostService) {
    route("/api/posts") {

        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
            call.respond(postService.getPosts(page, size))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                return@get
            }
            val post = postService.getPost(id)
            if (post != null) {
                call.respond(post)
            } else {
                call.respond(HttpStatusCode.NotFound, "Post not found")
            }
        }

        post {
            val request = call.receive<PostRequest>()
            if (request.title.isBlank() || request.content.isBlank() || request.author.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "All fields are required")
                return@post
            }
            val post = postService.createPost(request)
            call.respond(HttpStatusCode.Created, post)
        }

        post("/{id}/like") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                return@post
            }
            val post = postService.likePost(id)
            if (post != null) {
                call.respond(post)
            } else {
                call.respond(HttpStatusCode.NotFound, "Post not found")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                return@delete
            }
            val author = call.request.queryParameters["author"] ?: ""
            // Admin can delete any post, regular users can only delete their own
            val isAdmin = call.requireAdmin()
            if (isAdmin || postService.deletePost(id, author)) {
                call.respond(HttpStatusCode.OK, "Deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Post not found or author mismatch")
            }
        }
    }
}
