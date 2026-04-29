package com.example.jjjjjppppp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jjjjjppppp.network.ChatWebSocketManager
import com.example.jjjjjppppp.network.RetrofitClient
import com.example.jjjjjppppp.network.dto.ChatMessageDto
import com.example.jjjjjppppp.network.dto.ChatRequestDto
import com.example.jjjjjppppp.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val chatWsManager = ChatWebSocketManager()

    private val _messages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val messages: StateFlow<List<ChatMessageDto>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Error(val message: String) : UiState()
    }

    private val prefs by lazy {
        application.getSharedPreferences("user_accounts", Context.MODE_PRIVATE)
    }

    val currentUser: String
        get() = prefs.getString("current_user", "anonymous") ?: "anonymous"

    private var chatTarget: String = ""

    fun init(targetUser: String) {
        chatTarget = targetUser
        loadHistory()
        connectWebSocket()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getMessages(currentUser, chatTarget).fold(
                onSuccess = { msgs ->
                    _messages.value = msgs
                    _uiState.value = UiState.Idle
                },
                onFailure = { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to load")
                }
            )
        }
    }

    private fun connectWebSocket() {
        chatWsManager.onMessageReceived = { msg ->
            // Only add if it belongs to this conversation
            if ((msg.fromUser == currentUser && msg.toUser == chatTarget) ||
                (msg.fromUser == chatTarget && msg.toUser == currentUser)
            ) {
                _messages.value = _messages.value + msg
            }
        }
        chatWsManager.connect(RetrofitClient.BASE_URL, currentUser)
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            // Send via REST (more reliable), WebSocket is for receiving
            repository.sendMessage(currentUser, chatTarget, content).fold(
                onSuccess = { msg ->
                    _messages.value = _messages.value + msg
                },
                onFailure = {
                    // Try WebSocket as fallback
                    chatWsManager.sendMessage(ChatRequestDto(currentUser, chatTarget, content))
                }
            )
        }
    }

    fun disconnect() {
        chatWsManager.disconnect()
    }
}
