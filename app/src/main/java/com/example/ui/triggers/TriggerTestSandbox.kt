package com.example.ui.triggers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.SandboxSimulationResult
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary

@Composable
fun TriggerTestSandbox(
    simulationResult: SandboxSimulationResult?,
    onTest: (String) -> Unit,
    onClear: () -> Unit,
    onSendToChat: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var testInput by remember { mutableStateOf("") }

    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, BorderOutline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = LavenderSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trigger Test Sandbox",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Text(
                text = "Dry-run any message without sending to check trigger matching, rules, and random reply selection.",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Input field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = testInput,
                    onValueChange = {
                        testInput = it
                        onTest(it)
                    },
                    placeholder = { Text("e.g. Hi Nana, how are you?", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sandbox_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = BorderOutline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onTest(testInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    modifier = Modifier.testTag("test_rule_button")
                ) {
                    Text("Test", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Simulation Result Display
            if (simulationResult != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sandbox_result_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (simulationResult.matched) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (simulationResult.matched) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (simulationResult.matched) "MATCH FOUND!" else "NO TRIGGER MATCHED",
                                fontWeight = FontWeight.Bold,
                                color = if (simulationResult.matched) SuccessGreen else ErrorRed,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (simulationResult.matched) {
                            Text("Matched Trigger: ${simulationResult.triggerName}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Matched Keyword: '${simulationResult.matchedKeyword}'", color = LavenderSecondary, fontSize = 12.sp)
                            Text("Matching Mode: ${simulationResult.matchingMode}", color = TextSecondary, fontSize = 12.sp)
                            Text("Estimated Typing Delay: ${simulationResult.estimatedDelayMs} ms", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Simulated Bot Reply:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = simulationResult.selectedReply,
                                color = LavenderSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .padding(8.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onSendToChat(testInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Send to Live Chat", color = Color.White, fontSize = 12.sp)
                            }
                        } else {
                            Text(simulationResult.reason, color = TextMuted, fontSize = 12.sp)
                            if (simulationResult.fallbackReply != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Fallback Offline Response:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = simulationResult.fallbackReply,
                                    color = LavenderSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceElevated)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
