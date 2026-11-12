package com.example.ui.triggers

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.ResponseAlternativeEntity
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerListScreen(
    viewModel: AdminViewModel,
    onNavigateToChatWithText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val triggers by viewModel.triggers.collectAsStateWithLifecycle()
    val currentPersona by viewModel.currentPersona.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.triggerSearchQuery.collectAsStateWithLifecycle()
    val sandboxResult by viewModel.sandboxResult.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingTrigger by remember { mutableStateOf<KeywordTriggerEntity?>(null) }
    var editingAlternatives by remember { mutableStateOf<List<ResponseAlternativeEntity>>(emptyList()) }
    var showSandboxSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val categories = listOf("All", "Greetings", "Affection", "Emotions", "Questions", "Reactions", "Custom")

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
                            text = "Trigger Rules",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${currentPersona?.name ?: "Bot"} Persona • ${triggers.size} rules",
                            fontSize = 12.sp,
                            color = LavenderSecondary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSandboxSheet = !showSandboxSheet },
                        modifier = Modifier.testTag("open_sandbox_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Test Sandbox",
                            tint = LavenderSecondary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTrigger = null
                    editingAlternatives = emptyList()
                    showEditDialog = true
                },
                containerColor = VioletPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_trigger_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Trigger")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${triggers.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Total", fontSize = 11.sp, color = TextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${triggers.count { it.isEnabled }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LavenderSecondary)
                        Text("Active", fontSize = 11.sp, color = TextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${triggers.count { !it.isEnabled }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("Disabled", fontSize = 11.sp, color = TextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${categories.size - 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
                        Text("Categories", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            // Sandbox inline if open
            if (showSandboxSheet) {
                TriggerTestSandbox(
                    simulationResult = sandboxResult,
                    onTest = { viewModel.testSandboxInput(it) },
                    onClear = { viewModel.clearSandbox() },
                    onSendToChat = { text ->
                        showSandboxSheet = false
                        onNavigateToChatWithText(text)
                    },
                    onDismiss = { showSandboxSheet = false }
                )
            }

            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedCategory.value = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceDark,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) VioletPrimary else BorderOutline,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Trigger Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.triggerSearchQuery.value = it },
                placeholder = { Text("Filter triggers or keywords...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = VioletPrimary,
                    unfocusedBorderColor = BorderOutline,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            // Trigger Items List
            if (triggers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = LavenderSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "All") "No matching triggers found" else "No triggers configured",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap (+) below to create a custom trigger rule",
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
                        .testTag("trigger_list")
                ) {
                    items(triggers, key = { it.id }) { trigger ->
                        TriggerCardItem(
                            trigger = trigger,
                            onToggle = { viewModel.toggleTriggerEnabled(trigger) },
                            onEdit = {
                                scope.launch {
                                    val alts = viewModel.getAlternativesForTriggerSync(trigger.id)
                                    editingTrigger = trigger
                                    editingAlternatives = alts
                                    showEditDialog = true
                                }
                            },
                            onDuplicate = { viewModel.duplicateTrigger(trigger.id) },
                            onDelete = { viewModel.deleteTrigger(trigger.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                    }
                }
            }
        }
    }

    // Trigger Edit/Create Dialog
    if (showEditDialog) {
        val personaId = currentPersona?.id ?: 1L
        TriggerEditDialog(
            trigger = editingTrigger,
            initialAlternatives = editingAlternatives,
            personaId = personaId,
            onDismiss = { showEditDialog = false },
            onSave = { triggerToSave, altsToSave ->
                viewModel.saveTriggerWithAlternatives(triggerToSave, altsToSave)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun TriggerCardItem(
    trigger: KeywordTriggerEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onEdit() }
            .testTag("trigger_card_${trigger.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trigger.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Keyword: \"${trigger.primaryKeyword}\"",
                        fontSize = 13.sp,
                        color = LavenderSecondary
                    )
                }

                Switch(
                    checked = trigger.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary),
                    modifier = Modifier.testTag("trigger_switch_${trigger.id}")
                )

                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMuted)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Trigger") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Matching mode badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = trigger.matchingMode.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = trigger.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = LavenderSecondary
                    )
                }

                // Priority
                if (trigger.priority > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Priority ${trigger.priority}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = VioletPrimary
                        )
                    }
                }

                // Cooldown
                if (trigger.cooldownSeconds > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${trigger.cooldownSeconds}s cooldown",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
