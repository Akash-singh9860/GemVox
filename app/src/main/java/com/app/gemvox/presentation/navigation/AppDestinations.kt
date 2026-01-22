package com.app.gemvox.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestinations : NavKey {

    @Serializable
    data object Live : AppDestinations {
        const val label = "Live Voice"
        val icon = Icons.Default.Mic
    }

    @Serializable
    data object Chat : AppDestinations {
        const val label = "Chat"
        val icon = Icons.AutoMirrored.Filled.Chat
    }
}