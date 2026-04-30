package com.example.jjjjjppppp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jjjjjppppp.network.RetrofitClient
import com.example.jjjjjppppp.network.dto.HomeConfigDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _config = MutableStateFlow<HomeConfigDto?>(null)
    val config: StateFlow<HomeConfigDto?> = _config.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadConfig()
    }

    fun loadConfig() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.getHomeConfig()
                if (response.isSuccessful) {
                    _config.value = response.body()
                } else {
                    _error.value = "加载失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "网络连接失败"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
