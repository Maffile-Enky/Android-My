package com.example.jjjjjppppp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jjjjjppppp.network.RetrofitClient
import com.example.jjjjjppppp.network.WebSocketManager
import com.example.jjjjjppppp.network.dto.PostDto
import com.example.jjjjjppppp.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepository(application)
    private val webSocketManager = WebSocketManager()

    private val _posts = MutableStateFlow<List<PostDto>>(emptyList())
    val posts: StateFlow<List<PostDto>> = _posts.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _wsConnected = MutableStateFlow(false)
    val wsConnected: StateFlow<Boolean> = _wsConnected.asStateFlow()

    sealed class UiState {
        data object Loading : UiState()
        data object Empty : UiState()
        data object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    init {
        setupWebSocket()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getPosts().fold(
                onSuccess = { postList ->
                    _posts.value = postList
                    _uiState.value = if (postList.isEmpty()) UiState.Empty else UiState.Success
                },
                onFailure = { e ->
                    _uiState.value = UiState.Error(e.message ?: "Network error")
                }
            )
        }
    }

    fun createPost(author: String, title: String, content: String) {
        viewModelScope.launch {
            repository.createPost(author, title, content).fold(
                onSuccess = { newPost ->
                    _posts.value = listOf(newPost) + _posts.value
                    if (_posts.value.size == 1) _uiState.value = UiState.Success
                },
                onFailure = { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to create post")
                }
            )
        }
    }

    fun likePost(id: Long) {
        viewModelScope.launch {
            repository.likePost(id).fold(
                onSuccess = { updated ->
                    _posts.value = _posts.value.map { if (it.id == id) updated else it }
                },
                onFailure = { /* silently fail for likes */ }
            )
        }
    }

    fun connectWebSocket() {
        webSocketManager.connect(RetrofitClient.BASE_URL)
    }

    fun disconnectWebSocket() {
        webSocketManager.disconnect()
    }

    private fun setupWebSocket() {
        webSocketManager.onConnectionState = { connected ->
            _wsConnected.value = connected
        }
        webSocketManager.onNewPost = {
            // Refresh to get the new post from server
            loadPosts()
        }
        webSocketManager.onPostLiked = { postId, likeCount ->
            _posts.value = _posts.value.map {
                if (it.id == postId) it.copy(likes = likeCount) else it
            }
        }
        webSocketManager.onPostDeleted = { postId ->
            _posts.value = _posts.value.filter { it.id != postId }
        }
    }
}
