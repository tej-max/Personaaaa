package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.PersonaChatRepository
import com.example.engine.SchedulerManager
import com.example.ui.AdminViewModel
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.media.MediaLibraryScreen
import com.example.ui.navigation.Screen
import com.example.ui.schedule.ScheduleScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.PersonaChatTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VioletPrimary
import com.example.ui.triggers.TriggerListScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val repository = PersonaChatRepository(db.personaDao(), applicationContext)
        val schedulerManager = SchedulerManager(applicationContext)

        setContent {
            PersonaChatTheme(darkTheme = true) {
                PersonaChatApp(
                    repository = repository,
                    schedulerManager = schedulerManager
                )
            }
        }
    }
}

@Composable
fun PersonaChatApp(
    repository: PersonaChatRepository,
    schedulerManager: SchedulerManager
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Chat) }

    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.provideFactory(repository)
    )
    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.provideFactory(repository, schedulerManager)
    )

    // Request notification permission on Android 13+
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Handle back button: return to Chat tab if on another screen
    BackHandler(enabled = currentScreen != Screen.Chat) {
        currentScreen = Screen.Chat
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_navigation_bar")
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentScreen.route == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = VioletPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.Chat -> {
                ChatScreen(
                    viewModel = chatViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.Triggers -> {
                TriggerListScreen(
                    viewModel = adminViewModel,
                    onNavigateToChatWithText = { textToSend ->
                        currentScreen = Screen.Chat
                        chatViewModel.sendMessage(textToSend)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.Schedule -> {
                ScheduleScreen(
                    viewModel = adminViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.Media -> {
                MediaLibraryScreen(
                    viewModel = adminViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.Settings -> {
                SettingsScreen(
                    viewModel = adminViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
