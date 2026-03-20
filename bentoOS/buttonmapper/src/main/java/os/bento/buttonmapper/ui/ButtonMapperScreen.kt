package os.bento.buttonmapper.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.bento.buttonmapper.BentoAction
import os.bento.settings.ui.theme.BentoColors
import os.bento.settings.ui.theme.InterFamily
import os.bento.settings.ui.theme.MonoFamily

data class KeyMapping(
    val keycode: Int,
    val keyLabel: String,
    val action: BentoAction,
    val appPackage: String = "global",
    val appName: String = "System-wide",
)

val defaultMappings = listOf(
    KeyMapping(104, "Gamepad Start",  BentoAction.SPOTLIGHT,       appName = "System-wide"),
    KeyMapping(109, "Gamepad Select", BentoAction.MISSION_CONTROL, appName = "System-wide"),
    KeyMapping(131, "F1",             BentoAction.SPOTLIGHT),
    KeyMapping(132, "F2",             BentoAction.MISSION_CONTROL),
    KeyMapping(133, "F3",             BentoAction.SCREENSHOT),
    KeyMapping(134, "F4",             BentoAction.CONTROL_CENTER),
    KeyMapping(82,  "Menu",           BentoAction.CONTROL_CENTER),
)

@Composable
fun ButtonMapperScreen() {
    var mappings by remember { mutableStateOf(defaultMappings) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMapping by remember { mutableStateOf<KeyMapping?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(BentoColors.bg).padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Button Mapper", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = BentoColors.textPrimary)
                Text("Remap any key to any action", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textSecondary)
            }
            Button(onClick = { showAddDialog = true }, shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoColors.accent)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add", fontFamily = InterFamily, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(BentoColors.surface)
            .border(0.5.dp, BentoColors.border, RoundedCornerShape(8.dp)).padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf("System-wide", "Per-app").forEachIndexed { i, label ->
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTab == i) BentoColors.accentDim else Color.Transparent)
                    .clickable { selectedTab = i }.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(label, fontFamily = InterFamily, fontSize = 13.sp,
                        color = if (selectedTab == i) BentoColors.accent else BentoColors.textSecondary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(mappings) { mapping ->
                MappingRow(mapping = mapping, onEdit = { editingMapping = it }, onDelete = { mappings = mappings.filter { m -> m != it } })
            }
            item {
                if (mappings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No mappings yet — tap Add to create one", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textSecondary)
                    }
                }
            }
        }
    }

    if (showAddDialog || editingMapping != null) {
        MappingDialog(existing = editingMapping,
            onSave = { mapping ->
                if (editingMapping != null) mappings = mappings.map { if (it == editingMapping) mapping else it }
                else mappings = mappings + mapping
                showAddDialog = false; editingMapping = null
            },
            onDismiss = { showAddDialog = false; editingMapping = null })
    }
}

@Composable
fun MappingRow(mapping: KeyMapping, onEdit: (KeyMapping) -> Unit, onDelete: (KeyMapping) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BentoColors.surface)
        .border(0.5.dp, BentoColors.border, RoundedCornerShape(10.dp)).clickable { onEdit(mapping) }
        .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(BentoColors.surfaceElevated)
            .border(0.5.dp, BentoColors.borderBright, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(mapping.keyLabel, fontFamily = MonoFamily, fontSize = 12.sp, color = BentoColors.textPrimary)
        }
        Icon(Icons.Default.ArrowForward, null, tint = BentoColors.textSecondary, modifier = Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(mapping.action.label, fontFamily = InterFamily, fontSize = 14.sp, color = BentoColors.textPrimary)
            Text(mapping.appName, fontFamily = InterFamily, fontSize = 12.sp, color = BentoColors.textSecondary)
        }
        IconButton(onClick = { onDelete(mapping) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, null, tint = BentoColors.textSecondary, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun MappingDialog(existing: KeyMapping?, onSave: (KeyMapping) -> Unit, onDismiss: () -> Unit) {
    var keyLabel by remember { mutableStateOf(existing?.keyLabel ?: "") }
    var keycode by remember { mutableStateOf(existing?.keycode?.toString() ?: "") }
    var selectedAction by remember { mutableStateOf(existing?.action ?: BentoAction.NONE) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss, containerColor = BentoColors.surface, shape = RoundedCornerShape(14.dp),
        title = { Text(if (existing != null) "Edit Mapping" else "Add Mapping", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = BentoColors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = keyLabel, onValueChange = { keyLabel = it },
                    label = { Text("Key name (e.g. Gamepad Start)", fontFamily = InterFamily, fontSize = 12.sp) },
                    singleLine = true, shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoColors.accent,
                        unfocusedBorderColor = BentoColors.border, focusedContainerColor = BentoColors.surfaceElevated,
                        unfocusedContainerColor = BentoColors.surfaceElevated, focusedTextColor = BentoColors.textPrimary,
                        unfocusedTextColor = BentoColors.textPrimary), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = keycode, onValueChange = { keycode = it },
                    label = { Text("Keycode (Android KeyEvent int)", fontFamily = InterFamily, fontSize = 12.sp) },
                    singleLine = true, shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoColors.accent,
                        unfocusedBorderColor = BentoColors.border, focusedContainerColor = BentoColors.surfaceElevated,
                        unfocusedContainerColor = BentoColors.surfaceElevated, focusedTextColor = BentoColors.textPrimary,
                        unfocusedTextColor = BentoColors.textPrimary), modifier = Modifier.fillMaxWidth())
                Text("Action", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textSecondary)
                Box {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(BentoColors.surfaceElevated).border(0.5.dp, BentoColors.border, RoundedCornerShape(8.dp))
                        .clickable { expanded = true }.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedAction.label, fontFamily = InterFamily, fontSize = 14.sp, color = BentoColors.textPrimary)
                        Icon(Icons.Default.ExpandMore, null, tint = BentoColors.textSecondary, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier.background(BentoColors.surface)) {
                        BentoAction.values().forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.label, fontFamily = InterFamily, fontSize = 13.sp,
                                    color = if (action == selectedAction) BentoColors.accent else BentoColors.textPrimary) },
                                onClick = { selectedAction = action; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { val kc = keycode.toIntOrNull() ?: return@Button; onSave(KeyMapping(kc, keyLabel, selectedAction)) },
                shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = BentoColors.accent)) {
                Text("Save", fontFamily = InterFamily, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BentoColors.border)) {
                Text("Cancel", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textPrimary)
            }
        })
}
