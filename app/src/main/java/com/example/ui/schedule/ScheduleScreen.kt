package com.example.ui.schedule

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MessageType
import com.example.data.model.RepeatFrequency
import com.example.data.model.ScheduledMessageEntity
import com.example.ui.AdminViewModel
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.scheduledMessages.collectAsStateWithLifecycle()
    val currentPersona by viewModel.currentPersona.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<ScheduledMessageEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = "Scheduled Messages",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Native Android Alarms • Offline Local Delivery",
                            fontSize = 12.sp,
                            color = LavenderSecondary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingSchedule = null
                    showDialog = true
                },
                containerColor = VioletPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Explanatory Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Automated Persona Check-ins",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "At the configured time, ${currentPersona?.name ?: "the bot"} will automatically send the message directly into the chat and sound a local device notification.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = null,
                            tint = LavenderSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No scheduled messages",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap (+) below to schedule daily greetings or reminders",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("schedule_list")
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCardItem(
                            schedule = schedule,
                            onToggle = { viewModel.toggleSchedule(schedule) },
                            onEdit = {
                                editingSchedule = schedule
                                showDialog = true
                            },
                            onDelete = { viewModel.deleteSchedule(schedule.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showDialog) {
        val personaId = currentPersona?.id ?: 1L
        ScheduleEditDialog(
            schedule = editingSchedule,
            personaId = personaId,
            onDismiss = { showDialog = false },
            onSave = { savedSchedule ->
                viewModel.saveSchedule(savedSchedule)
                showDialog = false
            }
        )
    }
}

@Composable
fun ScheduleCardItem(
    schedule: ScheduledMessageEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", schedule.hour, schedule.minute)

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onEdit() }
            .testTag("schedule_card_${schedule.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = LavenderSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = schedule.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary)
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = schedule.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"${schedule.content}\"",
                fontSize = 13.sp,
                color = LavenderSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = schedule.repeatFrequency.replace("_", " "),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditDialog(
    schedule: ScheduledMessageEntity?,
    personaId: Long,
    onDismiss: () -> Unit,
    onSave: (ScheduledMessageEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(schedule?.name ?: "Daily Greeting") }
    var content by remember { mutableStateOf(schedule?.content ?: "Good morning Nana!") }
    var hour by remember { mutableIntStateOf(schedule?.hour ?: 8) }
    var minute by remember { mutableIntStateOf(schedule?.minute ?: 30) }
    var repeatFrequency by remember { mutableStateOf(schedule?.repeatFrequency ?: RepeatFrequency.DAILY.name) }
    var mediaUri by remember { mutableStateOf(schedule?.mediaUri) }

    val frequencies = RepeatFrequency.values()
    var freqExpanded by remember { mutableStateOf(false) }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) mediaUri = uri.toString()
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m ->
            hour = h
            minute = m
        },
        hour,
        minute,
        true
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SurfaceDark,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (schedule == null) "New Scheduled Message" else "Edit Schedule",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Schedule Label") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = BorderOutline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Message Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = BorderOutline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Time Picker Trigger Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .clickable { timePickerDialog.show() }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trigger Time:", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = freqExpanded,
                    onExpandedChange = { freqExpanded = !freqExpanded }
                ) {
                    OutlinedTextField(
                        value = frequencies.firstOrNull { it.name == repeatFrequency }?.displayName ?: repeatFrequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = BorderOutline
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = freqExpanded,
                        onDismissRequest = { freqExpanded = false }
                    ) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.displayName) },
                                onClick = {
                                    repeatFrequency = freq.name
                                    freqExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                val saved = (schedule ?: ScheduledMessageEntity(
                                    personaId = personaId,
                                    name = name.ifBlank { "Scheduled Message" },
                                    content = content,
                                    hour = hour,
                                    minute = minute
                                )).copy(
                                    name = name.ifBlank { "Scheduled Message" },
                                    content = content,
                                    hour = hour,
                                    minute = minute,
                                    repeatFrequency = repeatFrequency,
                                    mediaUri = mediaUri,
                                    isEnabled = true
                                )
                                onSave(saved)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                    ) {
                        Text("Save Schedule", color = Color.White)
                    }
                }
            }
        }
    }
}
