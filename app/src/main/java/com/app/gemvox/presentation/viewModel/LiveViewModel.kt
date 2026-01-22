package com.app.gemvox.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.app.gemvox.data.network.GeminiSocketClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LiveViewModel @Inject constructor(
    private val socketClient: GeminiSocketClient
) : ViewModel() {
    val errorMessage: StateFlow<String?> = socketClient.errorMessage
    fun clearError() = socketClient.clearError()
}