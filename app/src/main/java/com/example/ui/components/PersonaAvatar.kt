package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.VioletPrimary

@Composable
fun PersonaAvatar(
    name: String,
    avatarUri: String?,
    themeColor: Long,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "P"
    val baseColor = Color(themeColor)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(baseColor, baseColor.copy(alpha = 0.7f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "$name avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontSize = (size.value * 0.45f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
