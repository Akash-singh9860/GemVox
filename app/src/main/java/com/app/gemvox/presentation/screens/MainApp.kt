package com.app.gemvox.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.app.gemvox.presentation.navigation.AppDestinations

@Composable
fun MainApp() {
    val backStack = rememberNavBackStack(AppDestinations.Live)
    val onNavigateToTab: (AppDestinations) -> Unit = { destination ->
        backStack.clear()
        backStack.add(destination)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val currentKey = backStack.lastOrNull()
                val tabs = listOf(AppDestinations.Live, AppDestinations.Chat)
                tabs.forEach { tab ->
                    val isSelected = currentKey == tab
                    NavigationBarItem(
                        icon = {
                            val icon = when(tab) {
                                is AppDestinations.Live -> AppDestinations.Live.icon
                                is AppDestinations.Chat -> AppDestinations.Chat.icon
                            }
                            Icon(icon, contentDescription = null)
                        },
                        label = {
                            val label = when(tab) {
                                is AppDestinations.Live -> AppDestinations.Live.label
                                is AppDestinations.Chat -> AppDestinations.Chat.label
                            }
                            Text(label)
                        },
                        selected = isSelected,
                        onClick = { onNavigateToTab(tab) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            entryProvider = { key ->
                when (key) {
                    is AppDestinations.Live -> {
                        NavEntry(key) {
                            LiveScreen()
                        }
                    }
                    is AppDestinations.Chat -> {
                        NavEntry(key) {
                            ChatScreen()
                        }
                    }
                    else -> NavEntry(key) { Text("Unknown Screen") }
                }
            }
        )
    }
}