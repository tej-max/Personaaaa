package com.example.ui.triggers

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MatchingMode
import com.example.data.model.ResponseAlternativeEntity
import com.example.data.model.ResponseType
import com.example.ui.components.CoilGifImageLoader
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerEditDialog(
    trigger: KeywordTriggerEntity?,
    initialAlternatives: List<ResponseAlternativeEntity>,
    personaId: Long,
    onDismiss: () -> Unit,
    onSave: (KeywordTriggerEntity, List<ResponseAlternativeEntity>) -> Unit
) {
    var name by remember { mutableStateOf(trigger?.name ?: "") }
    var primaryKeyword by remember { mutableStateOf(trigger?.primaryKeyword ?: "") }
    var aliasesText by remember {
        val aliases = try {
            val arr = JSONArray(trigger?.aliasesJson ?: "[]")
            (0 until arr.length()).map { arr.getString(it) }.joinToString(", ")
        } catch (_: Exception) { "" }
        mutableStateOf(aliases)
    }
    var matchingMode by remember { mutableStateOf(trigger?.matchingMode ?: MatchingMode.WHOLE_WORD.name) }
    var category by remember { mutableStateOf(trigger?.category ?: "Greetings") }
    var caseSensitive by remember { mutableStateOf(trigger?.caseSensitive ?: false) }
    var ignorePunctuation by remember { mutableStateOf(trigger?.ignorePunctuation ?: true) }
    var priority by remember { mutableIntStateOf(trigger?.priority ?: 1) }
    var cooldownSeconds by remember { mutableLongStateOf(trigger?.cooldownSeconds ?: 0L) }
    var contextCondition by remember { mutableStateOf(trigger?.contextConditionBotContains ?: "") }

    val alternatives = remember {
        mutableStateListOf<ResponseAlternativeEntity>().apply {
            if (initialAlternatives.isNotEmpty()) {
                addAll(initialAlternatives)
            } else {
                add(ResponseAlternativeEntity(triggerId = 0, textContent = "Hi Nana!"))
            }
        }
    }

    var modeMenuExpanded by remember { mutableStateOf(false) }
    val modes = MatchingMode.values()
    val categories = listOf("Greetings", "Affection", "Emotions", "Questions", "Reactions", "Custom")
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    var mediaPickerIndex by remember { mutableStateOf<Int?>(null) }
    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        mediaPickerIndex?.let { index ->
            if (index < alternatives.size && uri != null) {
                val current = alternatives[index]
                val uriStr = uri.toString()
                val isGif = uriStr.contains("gif", ignoreCase = true)
                alternatives[index] = current.copy(
                    mediaUri = uriStr,
                    responseType = if (isGif) ResponseType.GIF.name else ResponseType.IMAGE.name
                )
            }
        }
        mediaPickerIndex = null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (trigger == null) "Create Trigger Rule" else "Edit Trigger Rule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    item {
                        // Trigger Name & Category
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Trigger Name (e.g. Greeting)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trigger_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderOutline
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryMenuExpanded,
                            onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
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
                                expanded = categoryMenuExpanded,
                                onDismissRequest = { categoryMenuExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Primary Keyword
                        OutlinedTextField(
                            value = primaryKeyword,
                            onValueChange = { primaryKeyword = it },
                            label = { Text("Primary Keyword or Phrase (e.g. Hi)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("primary_keyword_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderOutline
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Aliases
                        OutlinedTextField(
                            value = aliasesText,
                            onValueChange = { aliasesText = it },
                            label = { Text("Aliases (comma separated, e.g. hello, hey, howdy)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderOutline
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Matching Mode Selector
                        Text("Matching Mode", color = LavenderSecondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = modeMenuExpanded,
                            onExpandedChange = { modeMenuExpanded = !modeMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = modes.firstOrNull { it.name == matchingMode }?.displayName ?: matchingMode,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeMenuExpanded) },
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
                                expanded = modeMenuExpanded,
                                onDismissRequest = { modeMenuExpanded = false }
                            ) {
                                modes.forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(mode.displayName, fontWeight = FontWeight.SemiBold)
                                                Text(mode.description, fontSize = 11.sp, color = TextMuted)
                                            }
                                        },
                                        onClick = {
                                            matchingMode = mode.name
                                            modeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Toggles: Case Sensitive, Ignore Punctuation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ignore Punctuation", color = Color.White, fontSize = 14.sp)
                            Switch(
                                checked = ignorePunctuation,
                                onCheckedChange = { ignorePunctuation = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Case Sensitive Matching", color = Color.White, fontSize = 14.sp)
                            Switch(
                                checked = caseSensitive,
                                onCheckedChange = { caseSensitive = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary)
                            )
                        }

                        // Priority & Cooldown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = priority.toString(),
                                onValueChange = { priority = it.toIntOrNull() ?: 1 },
                                label = { Text("Priority (1-100)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = VioletPrimary,
                                    unfocusedBorderColor = BorderOutline
                                )
                            )
                            OutlinedTextField(
                                value = cooldownSeconds.toString(),
                                onValueChange = { cooldownSeconds = it.toLongOrNull() ?: 0L },
                                label = { Text("Cooldown (sec)") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = VioletPrimary,
                                    unfocusedBorderColor = BorderOutline
                                )
                            )
                        }

                        // Context Condition
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contextCondition,
                            onValueChange = { contextCondition = it },
                            label = { Text("Optional Context: Bot's previous response contains...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderOutline
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Alternatives Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alternative Replies (${alternatives.size})",
                                color = LavenderSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            TextButton(
                                onClick = {
                                    alternatives.add(
                                        ResponseAlternativeEntity(
                                            triggerId = trigger?.id ?: 0,
                                            textContent = ""
                                        )
                                    )
                                },
                                modifier = Modifier.testTag("add_alternative_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = VioletPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Reply", color = VioletPrimary)
                            }
                        }
                    }

                    // Alternatives List Items
                    itemsIndexed(alternatives) { index, alt ->
                        Surface(
                            color = SurfaceVariantDark,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Alternative #${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                    if (alternatives.size > 1) {
                                        IconButton(
                                            onClick = { alternatives.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete alternative", tint = ErrorRed)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = alt.textContent,
                                    onValueChange = { newText ->
                                        alternatives[index] = alt.copy(textContent = newText)
                                    },
                                    placeholder = { Text("Enter bot response...", color = TextMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("alternative_input_$index"),
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = VioletPrimary,
                                        unfocusedBorderColor = BorderOutline
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Attach Image or GIF
                                    if (!alt.mediaUri.isNullOrBlank()) {
                                        val context = LocalContext.current
                                        val loader = remember { CoilGifImageLoader.get(context) }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(alt.mediaUri)
                                                    .crossfade(true)
                                                    .build(),
                                                imageLoader = loader,
                                                contentDescription = "Alternative media",
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            IconButton(onClick = {
                                                alternatives[index] = alt.copy(mediaUri = null, responseType = ResponseType.TEXT.name)
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove media", tint = TextMuted, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    } else {
                                        TextButton(onClick = {
                                            mediaPickerIndex = index
                                            mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                        }) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = LavenderSecondary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Attach Image/GIF", color = LavenderSecondary, fontSize = 12.sp)
                                        }
                                    }

                                    // Sequence delay
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Delay: ", fontSize = 11.sp, color = TextMuted)
                                        OutlinedTextField(
                                            value = alt.delaySeconds.toString(),
                                            onValueChange = {
                                                val d = it.toFloatOrNull() ?: 1.5f
                                                alternatives[index] = alt.copy(delaySeconds = d)
                                            },
                                            modifier = Modifier.width(70.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = VioletPrimary,
                                                unfocusedBorderColor = BorderOutline
                                            )
                                        )
                                        Text("s", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(start = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (primaryKeyword.isNotBlank()) {
                                val aliasesArray = JSONArray()
                                aliasesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                                    aliasesArray.put(it)
                                }
                                val updatedTrigger = (trigger ?: KeywordTriggerEntity(
                                    personaId = personaId,
                                    name = name.ifBlank { primaryKeyword },
                                    primaryKeyword = primaryKeyword
                                )).copy(
                                    name = name.ifBlank { primaryKeyword },
                                    category = category,
                                    primaryKeyword = primaryKeyword.trim(),
                                    aliasesJson = aliasesArray.toString(),
                                    matchingMode = matchingMode,
                                    caseSensitive = caseSensitive,
                                    ignorePunctuation = ignorePunctuation,
                                    priority = priority,
                                    cooldownSeconds = cooldownSeconds,
                                    contextConditionBotContains = contextCondition.ifBlank { null }
                                )
                                onSave(updatedTrigger, alternatives)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        modifier = Modifier.testTag("save_trigger_button")
                    ) {
                        Text("Save Trigger", color = Color.White)
                    }
                }
            }
        }
    }
}
