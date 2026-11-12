package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MessageType
import com.example.ui.theme.BotBubbleBg
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.UserBubbleBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessageEntity,
    botAvatarUri: String?,
    botName: String,
    onDeleteMessage: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
    }

    val bubbleBg = if (isUser) UserBubbleBg else BotBubbleBg
    val textColor = if (isUser) Color.White else Color(0xFFEDE9FE)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            PersonaAvatar(
                name = botName,
                avatarUri = botAvatarUri,
                themeColor = 0xFFA78BFA,
                size = 28.dp,
                modifier = Modifier.padding(end = 8.dp, bottom = 2.dp)
            )
        }

        Box {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag(if (isUser) "user_message_bubble" else "bot_message_bubble")
            ) {
                // Media (Image or animated GIF)
                if (!message.mediaUri.isNullOrBlank()) {
                    val context = LocalContext.current
                    val imageLoader = remember { CoilGifImageLoader.get(context) }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(message.mediaUri)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "Message media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    if (message.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // Text Content
                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }

                // Timestamp and Delivery Status
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.75f) else TextMuted
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Long-press Context Menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (message.content.isNotBlank()) {
                    DropdownMenuItem(
                        text = { Text("Copy Text") },
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            showMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete Message") },
                    onClick = {
                        onDeleteMessage(message.id)
                        showMenu = false
                    }
                )
            }
        }
    }
}
