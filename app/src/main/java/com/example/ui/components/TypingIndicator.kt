package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BotBubbleBg
import com.example.ui.theme.LavenderSecondary
import kotlinx.coroutines.delay

@Composable
fun TypingIndicator(
    botName: String = "Bot",
    modifier: Modifier = Modifier
) {
    val dotCount = 3
    val animatables = remember { List(dotCount) { Animatable(0f) } }

    animatables.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 160L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000
                        0.0f at 0 using LinearOutSlowInEasing
                        1.0f at 250 using LinearOutSlowInEasing
                        0.0f at 500 using LinearOutSlowInEasing
                        0.0f at 1000 using LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .testTag("typing_indicator")
            .semantics { contentDescription = "$botName is typing a response" }
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                .background(BotBubbleBg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                animatables.forEach { anim ->
                    val offsetY = -anim.value * 6.dp.value
                    Box(
                        modifier = Modifier
                            .offset(y = offsetY.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LavenderSecondary)
                    )
                }
            }
        }
    }
}
