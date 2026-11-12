package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Chat : Screen("chat", "Chat", Icons.Filled.Chat, Icons.Outlined.Chat)
    data object Triggers : Screen("triggers", "Triggers", Icons.Filled.ElectricBolt, Icons.Outlined.ElectricBolt)
    data object Schedule : Screen("schedule", "Schedule", Icons.Filled.Alarm, Icons.Outlined.Alarm)
    data object Media : Screen("media", "Media", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Chat, Triggers, Schedule, Media, Settings)
    }
}
