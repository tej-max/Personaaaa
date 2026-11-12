package com.example.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BotPersonaEntity
import com.example.data.model.FallbackMode
import com.example.data.model.TypingMode
import com.example.data.model.UserProfileEntity
import com.example.ui.AdminViewModel
import com.example.ui.components.PersonaAvatar
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentPersona by viewModel.currentPersona.collectAsStateWithLifecycle()
    val allPersonas by viewModel.allPersonas.collectAsStateWithLifecycle()

    var showEditUserDialog by remember { mutableStateOf(false) }
    var showEditBotDialog by remember { mutableStateOf(false) }
    var editingBot by remember { mutableStateOf<BotPersonaEntity?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonContent by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonContent by remember { mutableStateOf("") }

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
                    Text(
                        text = "Profiles & Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // SECTION 1: MY PROFILE (User)
                Text("MY PROFILE", color = LavenderSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PersonaAvatar(
                            name = userProfile?.displayName ?: "Me",
                            avatarUri = userProfile?.avatarUri,
                            themeColor = userProfile?.themeColorHex ?: 0xFF8B5CF6,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(userProfile?.displayName ?: "Me", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(userProfile?.about ?: "Available", color = TextMuted, fontSize = 12.sp)
                        }
                        IconButton(onClick = { showEditUserDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = LavenderSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 2: ACTIVE BOT PERSONA & PERSONA SWITCHER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("BOT PERSONAS (${allPersonas.size})", color = LavenderSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        editingBot = null
                        showEditBotDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Persona", color = VioletPrimary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Personas list
                allPersonas.forEach { bot ->
                    val isCurrent = bot.id == currentPersona?.id
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isCurrent) SurfaceVariantDark else SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.switchPersona(bot.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PersonaAvatar(
                                name = bot.name,
                                avatarUri = bot.avatarUri,
                                themeColor = bot.themeColorHex,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(bot.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(VioletPrimary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Active", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(bot.about, color = TextMuted, fontSize = 12.sp, maxLines = 1)
                            }
                            IconButton(onClick = {
                                editingBot = bot
                                showEditBotDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Persona", tint = TextMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 3: BACKUP & RESTORE
                Text("DATA BACKUP & RESTORE", color = LavenderSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        val json = viewModel.exportJson()
                                        exportJsonContent = json
                                        showExportDialog = true
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = VioletPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Export Configuration JSON", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Export triggers, rules, and persona config to backup", color = TextMuted, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    importJsonContent = ""
                                    showImportDialog = true
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = LavenderSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Import Configuration JSON", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Restore or import trigger rules from JSON text", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 4: OFFLINE GUARANTEE & APP INFO
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("100% Offline & Private", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PersonaChat contains ZERO AI models, cloud language processors, or tracking. Every response comes exclusively from deterministic keyword triggers, multi-step sequences, and schedules you configure.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Edit User Dialog
    if (showEditUserDialog) {
        val initialUser = userProfile ?: UserProfileEntity()
        var name by remember { mutableStateOf(initialUser.displayName) }
        var about by remember { mutableStateOf(initialUser.about) }
        var avatarUri by remember { mutableStateOf(initialUser.avatarUri) }

        val pfpPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) avatarUri = uri.toString()
        }

        AlertDialog(
            onDismissRequest = { showEditUserDialog = false },
            title = { Text("Edit My Profile", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = BorderOutline
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = about,
                        onValueChange = { about = it },
                        label = { Text("Status / About") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = BorderOutline
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = {
                        pfpPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text("Change Profile Picture", color = LavenderSecondary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveUserProfile(
                        initialUser.copy(displayName = name, about = about, avatarUri = avatarUri)
                    )
                    showEditUserDialog = false
                }) {
                    Text("Save", color = VioletPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUserDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Edit/Create Bot Dialog
    if (showEditBotDialog) {
        val initialBot = editingBot ?: BotPersonaEntity(name = "New Bot")
        var name by remember { mutableStateOf(initialBot.name) }
        var about by remember { mutableStateOf(initialBot.about) }
        var avatarUri by remember { mutableStateOf(initialBot.avatarUri) }
        var typingMode by remember { mutableStateOf(initialBot.typingMode) }
        var charsPerSecond by remember { mutableIntStateOf(initialBot.charsPerSecond) }
        var minDelay by remember { mutableFloatStateOf(initialBot.minDelaySeconds) }
        var maxDelay by remember { mutableFloatStateOf(initialBot.maxDelaySeconds) }
        var fixedDelay by remember { mutableFloatStateOf(initialBot.fixedDelaySeconds) }
        var fallbackMode by remember { mutableStateOf(initialBot.noMatchFallbackMode) }
        var avoidRepeat by remember { mutableStateOf(initialBot.avoidImmediateRepeat) }
        var enableTyping by remember { mutableStateOf(initialBot.enableTypingIndicator) }

        val pfpPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) avatarUri = uri.toString()
        }

        Dialog(onDismissRequest = { showEditBotDialog = false }) {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.padding(18.dp)) {
                    item {
                        Text(
                            text = if (editingBot == null) "Create Bot Persona" else "Edit ${initialBot.name}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Bot Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderOutline
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = about,
                            onValueChange = { about = it },
                            label = { Text("About / Personality Description") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderOutline
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = {
                            pfpPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Text("Select Bot Avatar", color = LavenderSecondary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Typing Simulation Behaviour", color = LavenderSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Typing Mode options
                        TypingMode.values().forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { typingMode = mode.name }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (typingMode == mode.name) VioletPrimary else SurfaceElevated)
                                        .border(2.dp, BorderOutline, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(mode.displayName, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (typingMode == "CHARACTER_BASED") {
                            Text("Typing Speed: $charsPerSecond chars/sec", color = TextSecondary, fontSize = 12.sp)
                            Slider(
                                value = charsPerSecond.toFloat(),
                                onValueChange = { charsPerSecond = it.toInt() },
                                valueRange = 10f..60f,
                                colors = SliderDefaults.colors(thumbColor = VioletPrimary, activeTrackColor = VioletPrimary)
                            )
                        } else if (typingMode == "FIXED_DELAY") {
                            Text("Delay: ${String.format("%.1f", fixedDelay)} seconds", color = TextSecondary, fontSize = 12.sp)
                            Slider(
                                value = fixedDelay,
                                onValueChange = { fixedDelay = it },
                                valueRange = 0.5f..8f,
                                colors = SliderDefaults.colors(thumbColor = VioletPrimary, activeTrackColor = VioletPrimary)
                            )
                        }

                        // Delay clamps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Typing Indicators (3 Dots)", color = Color.White, fontSize = 13.sp)
                            Switch(
                                checked = enableTyping,
                                onCheckedChange = { enableTyping = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Avoid Immediate Response Repeat", color = Color.White, fontSize = 13.sp)
                            Switch(
                                checked = avoidRepeat,
                                onCheckedChange = { avoidRepeat = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No-Match Fallback Behaviour", color = LavenderSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        FallbackMode.values().forEach { fb ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { fallbackMode = fb.name }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (fallbackMode == fb.name) VioletPrimary else SurfaceElevated)
                                        .border(2.dp, BorderOutline, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(fb.displayName, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEditBotDialog = false }) {
                                Text("Cancel", color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        val updated = initialBot.copy(
                                            name = name,
                                            about = about,
                                            avatarUri = avatarUri,
                                            typingMode = typingMode,
                                            charsPerSecond = charsPerSecond,
                                            fixedDelaySeconds = fixedDelay,
                                            minDelaySeconds = minDelay,
                                            maxDelaySeconds = maxDelay,
                                            noMatchFallbackMode = fallbackMode,
                                            avoidImmediateRepeat = avoidRepeat,
                                            enableTypingIndicator = enableTyping
                                        )
                                        viewModel.saveBotPersona(updated)
                                        showEditBotDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                            ) {
                                Text("Save Persona", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Configuration JSON", color = Color.White) },
            text = {
                Column {
                    Text("Copy this JSON configuration to backup your triggers and settings:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportJsonContent,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = BorderOutline
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(exportJsonContent))
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) {
                    Text("Copy to Clipboard", color = VioletPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Configuration JSON", color = Color.White) },
            text = {
                Column {
                    Text("Paste a PersonaChat configuration JSON below:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonContent,
                        onValueChange = { importJsonContent = it },
                        placeholder = { Text("Paste JSON here...", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = VioletPrimary,
                            unfocusedBorderColor = BorderOutline
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val success = viewModel.importJson(importJsonContent)
                        if (success) {
                            Toast.makeText(context, "Triggers imported successfully!", Toast.LENGTH_SHORT).show()
                            showImportDialog = false
                        } else {
                            Toast.makeText(context, "Failed to parse JSON", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Import", color = VioletPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
