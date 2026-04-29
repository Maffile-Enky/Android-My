package com.example.jjjjjppppp.repository

import android.content.Context
import com.example.jjjjjppppp.network.RetrofitClient
import com.example.jjjjjppppp.network.dto.PostDto
import com.example.jjjjjppppp.network.dto.PostRequestDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

class PostRepository(private val context: Context) {

    private val prefs by lazy { context.getSharedPreferences("community_prefs", Context.MODE_PRIVATE) }
    private val gson = Gson()

    suspend fun getPosts(page: Int = 0, size: Int = 20): Result<List<PostDto>> {
        return try {
            val response = RetrofitClient.apiService.getPosts(page, size)
            if (response.isSuccessful) {
                val posts = response.body()?.posts ?: emptyList()
                cachePosts(posts)
                Result.success(posts)
            } else {
                // Fallback to cache on error
                val cached = loadCachedPosts()
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            val cached = loadCachedPosts()
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }

    suspend fun createPost(author: String, title: String, content: String): Result<PostDto> {
        return try {
            val response = RetrofitClient.apiService.createPost(PostRequestDto(author, title, content))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likePost(id: Long): Result<PostDto> {
        return try {
            val response = RetrofitClient.apiService.likePost(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cachePosts(posts: List<PostDto>) {
        val json = gson.toJson(posts)
        prefs.edit().putString("cached_posts", json).apply()
    }

    fun loadCachedPosts(): List<PostDto> {
        val json = prefs.getString("cached_posts", null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<PostDto>>() {}.type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Legacy: load old SharedPreferences data formatted as JSONArray (for migration)
    fun hasLegacyData(): Boolean {
        return prefs.getString("forum_posts", null) != null
    }

    fun loadLegacyPosts(): List<PostDto> {
        val json = prefs.getString("forum_posts", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                PostDto(
                    id = obj.getLong("id"),
                    author = obj.getString("authorName"),
                    title = obj.getString("title"),
                    content = obj.getString("content"),
                    likes = obj.getInt("likeCount"),
                    createdAt = obj.getString("time")
                )
            }
        } catch (_: Exception) { emptyList() }
    }
}
