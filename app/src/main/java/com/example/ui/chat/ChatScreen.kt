package com.example.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ResponseAlternativeEntity
import com.example.ui.components.CoilGifImageLoader
import com.example.ui.components.DateSeparator
import com.example.ui.components.MessageBubble
import com.example.ui.components.PersonaAvatar
import com.example.ui.components.TypingIndicator
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UserBubbleBg
import com.example.ui.theme.VioletPrimary
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var attachedMediaUri by remember { mutableStateOf<String?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Photo & GIF Picker launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedMediaUri = uri.toString()
        }
    }

    // Scroll to bottom on new messages or typing state change
    LaunchedEffect(uiState.messages.size, uiState.isBotTyping) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PersonaAvatar(
                            name = uiState.currentPersona?.name ?: "Persona",
                            avatarUri = uiState.currentPersona?.avatarUri,
                            themeColor = uiState.currentPersona?.themeColorHex ?: 0xFFA78BFA,
                            size = 38.dp
                        )
                        Column {
                            Text(
                                text = uiState.currentPersona?.name ?: "PersonaChat",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = if (uiState.isBotTyping) "typing..." else "Offline • Deterministic Rules",
                                fontSize = 12.sp,
                                color = if (uiState.isBotTyping) LavenderSecondary else TextMuted
                            )
                        }
                    }
                },
                actions = {
                    // Manual Trigger Action
                    IconButton(
                        onClick = { viewModel.setManualDialogVisible(true) },
                        modifier = Modifier.testTag("manual_reply_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Manual Bot Reply",
                            tint = LavenderSecondary
                        )
                    }

                    // Search button
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search messages",
                            tint = Color.White
                        )
                    }

                    // Overflow Menu
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear Chat") },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar if active
            AnimatedVisibility(visible = uiState.isSearchOpen) {
                Surface(
                    color = SurfaceDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search messages in chat...", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("chat_search_input"),
                        singleLine = true,
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.White)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = BorderOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Message List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            PersonaAvatar(
                                name = uiState.currentPersona?.name ?: "Bot",
                                avatarUri = uiState.currentPersona?.avatarUri,
                                themeColor = uiState.currentPersona?.themeColorHex ?: 0xFFA78BFA,
                                size = 64.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Start chatting with ${uiState.currentPersona?.name ?: "your bot"}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try typing: 'Hi', 'How are you', 'Hug', or 'Joke'",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_message_list")
                    ) {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            // Optional date separator if day changed from previous message
                            val index = uiState.messages.indexOf(message)
                            val showDate = if (index == 0) {
                                true
                            } else {
                                val prev = uiState.messages[index - 1]
                                !isSameDay(prev.timestamp, message.timestamp)
                            }

                            if (showDate) {
                                DateSeparator(timestamp = message.timestamp)
                            }

                            MessageBubble(
                                message = message,
                                botAvatarUri = uiState.currentPersona?.avatarUri,
                                botName = uiState.currentPersona?.name ?: "Bot",
                                onDeleteMessage = { viewModel.deleteMessage(it) }
                            )
                        }

                        // Bottom typing indicator
                        if (uiState.isBotTyping) {
                            item(key = "typing_indicator") {
                                TypingIndicator(botName = uiState.currentPersona?.name ?: "Bot")
                            }
                        }
                    }
                }
            }

            // Attached Media Preview
            if (attachedMediaUri != null) {
                val context = LocalContext.current
                val imageLoader = remember { CoilGifImageLoader.get(context) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(attachedMediaUri)
                                .crossfade(true)
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = "Attached preview",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .clickable { attachedMediaUri = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove attached media",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Media attached",
                        color = LavenderSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Message Input Bar
            Surface(
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment button
                    IconButton(
                        onClick = {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier.testTag("attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach photo or GIF",
                            tint = LavenderSecondary
                        )
                    }

                    // Input Text Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Message ${uiState.currentPersona?.name ?: "Nana"}...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .testTag("message_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariantDark,
                            unfocusedContainerColor = SurfaceVariantDark,
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = BorderOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Send Button
                    val canSend = inputText.isNotBlank() || attachedMediaUri != null
                    IconButton(
                        onClick = {
                            if (canSend) {
                                viewModel.sendMessage(inputText, attachedMediaUri)
                                inputText = ""
                                attachedMediaUri = null
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (canSend) UserBubbleBg else SurfaceElevated)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = if (canSend) Color.White else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Clear Chat Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Chat History?", color = Color.White) },
            text = { Text("This will remove all messages with this persona from local storage. Triggers and settings are kept.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearChat()
                    showClearDialog = false
                }) {
                    Text("Clear", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = LavenderSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Manual Response Selector Modal
    if (uiState.showManualDialog) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setManualDialogVisible(false) },
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Manual Bot Reply",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Select any trigger response to manually send it into the chat immediately.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (uiState.triggersWithAlternatives.isEmpty()) {
                    Text(
                        "No triggers configured yet for this persona.",
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        items(uiState.triggersWithAlternatives) { (trigger, alts) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${trigger.name} (Keyword: ${trigger.primaryKeyword})",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = LavenderSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                alts.forEach { alt ->
                                    Surface(
                                        color = SurfaceVariantDark,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable { viewModel.sendManualResponse(alt) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = alt.textContent.ifBlank { "[${alt.responseType}]" },
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Send",
                                                tint = VioletPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
