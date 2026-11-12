package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateSeparator(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val dateText = remember(timestamp) {
        val messageCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance()

        if (messageCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            messageCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        ) {
            "Today"
        } else {
            nowCal.add(Calendar.DAY_OF_YEAR, -1)
            if (messageCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                messageCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
            ) {
                "Yesterday"
            } else {
                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = dateText,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}
