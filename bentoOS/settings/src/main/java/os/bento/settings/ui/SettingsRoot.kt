package os.bento.settings.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.bento.settings.ui.theme.BentoColors
import os.bento.settings.ui.theme.InterFamily
import os.bento.settings.ui.theme.MonoFamily

data class SettingsSection(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val accent: Color = BentoColors.accent,
)

val settingsSections = listOf(
    SettingsSection("wifi",       Icons.Default.Wifi,           "Wi-Fi"),
    SettingsSection("bluetooth",  Icons.Default.Bluetooth,      "Bluetooth"),
    SettingsSection("vpn",        Icons.Default.VpnKey,         "VPN",         Color(0xFF34C759)),
    SettingsSection("display",    Icons.Default.Tv,             "Display & HDR"),
    SettingsSection("sound",      Icons.Default.VolumeUp,       "Sound"),
    SettingsSection("privacy",    Icons.Default.Security,       "Privacy & Safety", Color(0xFFFF9500)),
    SettingsSection("gaming",     Icons.Default.SportsEsports,  "Gaming Mode",  Color(0xFF00FF88)),
    SettingsSection("dns",        Icons.Default.Block,          "DNS Ad Blocker"),
    SettingsSection("desktop",    Icons.Default.Dashboard,      "Desktop & Dock"),
    SettingsSection("appearance", Icons.Default.Palette,        "Appearance"),
    SettingsSection("apps",       Icons.Default.Apps,           "Apps"),
    SettingsSection("storage",    Icons.Default.Storage,        "Storage"),
    SettingsSection("updates",    Icons.Default.SystemUpdate,   "System Updates"),
    SettingsSection("developer",  Icons.Default.Code,           "Developer"),
    SettingsSection("about",      Icons.Default.Info,           "About BentoOS"),
)

@Composable
fun SettingsRoot() {
    var selected by remember { mutableStateOf("wifi") }
    Row(modifier = Modifier.fillMaxSize().background(BentoColors.bg)) {
        SettingsSidebar(sections = settingsSections, selected = selected, onSelect = { selected = it })
        Box(modifier = Modifier.fillMaxHeight().width(0.5.dp).background(BentoColors.border))
        Box(modifier = Modifier.fillMaxSize().background(BentoColors.bg)) {
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it / 10 } togetherWith
                    fadeOut() + slideOutHorizontally { -it / 10 }
                }
            ) { section -> SettingsContent(section) }
        }
    }
}

@Composable
fun SettingsSidebar(sections: List<SettingsSection>, selected: String, onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier.width(220.dp).fillMaxHeight()
            .background(BentoColors.surface).padding(vertical = 16.dp)
    ) {
        Text("Settings", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp, color = BentoColors.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp)) {
            items(sections) { section ->
                SidebarItem(section = section, isSelected = section.id == selected, onClick = { onSelect(section.id) })
            }
        }
    }
}

@Composable
fun SidebarItem(section: SettingsSection, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) BentoColors.accentDim else Color.Transparent
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(bg).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp))
                .background(section.accent.copy(alpha = if (isSelected) 0.2f else 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = section.icon, contentDescription = section.label,
                tint = section.accent, modifier = Modifier.size(15.dp))
        }
        Text(text = section.label, fontFamily = InterFamily, fontSize = 13.sp,
            color = if (isSelected) BentoColors.textPrimary else BentoColors.textSecondary,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
    }
}

@Composable
fun SettingsContent(section: String) {
    when (section) {
        "wifi"       -> WifiSettings()
        "bluetooth"  -> BluetoothSettings()
        "vpn"        -> VpnSettings()
        "display"    -> DisplaySettings()
        "sound"      -> SoundSettings()
        "privacy"    -> PrivacySettings()
        "gaming"     -> GamingSettings()
        "dns"        -> DnsSettings()
        "desktop"    -> DesktopSettings()
        "appearance" -> AppearanceSettings()
        "apps"       -> AppsSettings()
        "storage"    -> StorageSettings()
        "updates"    -> UpdateSettings()
        "developer"  -> DeveloperSettings()
        "about"      -> AboutSettings()
    }
}

@Composable
fun SettingsPage(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
        Text(title, fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp, color = BentoColors.textPrimary,
            modifier = Modifier.padding(bottom = 24.dp))
        content()
    }
}

@Composable
fun SettingsGroup(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column {
        if (title != null) {
            Text(title.uppercase(), fontFamily = InterFamily, fontSize = 11.sp,
                fontWeight = FontWeight.Medium, color = BentoColors.textSecondary,
                letterSpacing = 0.8.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
        }
        Surface(shape = RoundedCornerShape(12.dp), color = BentoColors.surface, tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().border(0.5.dp, BentoColors.border, RoundedCornerShape(12.dp))) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector? = null, iconTint: Color = BentoColors.accent,
    label: String, subtitle: String? = null,
    checked: Boolean, onCheckedChange: (Boolean) -> Unit, showDivider: Boolean = true
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontFamily = InterFamily, fontSize = 14.sp, color = BentoColors.textPrimary)
                if (subtitle != null) Text(subtitle, fontFamily = InterFamily, fontSize = 12.sp, color = BentoColors.textSecondary)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                    checkedTrackColor = BentoColors.accent, uncheckedThumbColor = BentoColors.textSecondary,
                    uncheckedTrackColor = BentoColors.surfaceElevated))
        }
        if (showDivider) Divider(color = BentoColors.border, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
fun SettingsNavRow(
    icon: ImageVector? = null, iconTint: Color = BentoColors.accent,
    label: String, subtitle: String? = null, badge: String? = null,
    showDivider: Boolean = true, onClick: () -> Unit = {}
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontFamily = InterFamily, fontSize = 14.sp, color = BentoColors.textPrimary)
                if (subtitle != null) Text(subtitle, fontFamily = InterFamily, fontSize = 12.sp, color = BentoColors.textSecondary)
            }
            if (badge != null) {
                Surface(color = BentoColors.accent, shape = CircleShape, modifier = Modifier.padding(end = 8.dp)) {
                    Text(badge, fontFamily = InterFamily, fontSize = 11.sp, color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = BentoColors.textSecondary, modifier = Modifier.size(16.dp))
        }
        if (showDivider) Divider(color = BentoColors.border, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable fun WifiSettings() = SettingsPage("Wi-Fi") {
    var enabled by remember { mutableStateOf(true) }
    SettingsGroup { SettingsToggleRow(Icons.Default.Wifi, BentoColors.accent, "Wi-Fi", checked = enabled, onCheckedChange = { enabled = it }, showDivider = false) }
    if (enabled) {
        SettingsGroup("Available Networks") {
            listOf("BentoNet_5G" to true, "HomeNetwork" to false, "AndroidTV_2.4" to false).forEachIndexed { i, (name, connected) ->
                SettingsNavRow(label = name, subtitle = if (connected) "Connected" else null,
                    icon = Icons.Default.Wifi, iconTint = if (connected) BentoColors.success else BentoColors.textSecondary, showDivider = i < 2)
            }
        }
    }
}

@Composable fun BluetoothSettings() = SettingsPage("Bluetooth") {
    var enabled by remember { mutableStateOf(false) }
    SettingsGroup { SettingsToggleRow(Icons.Default.Bluetooth, Color(0xFF007AFF), "Bluetooth", checked = enabled, onCheckedChange = { enabled = it }, showDivider = false) }
    if (enabled) { SettingsGroup("My Devices") { SettingsNavRow(Icons.Default.Gamepad, BentoColors.textSecondary, "BentoOS Gamepad", "Not paired", showDivider = false) } }
}

@Composable fun VpnSettings() = SettingsPage("VPN") {
    var vpnEnabled by remember { mutableStateOf(false) }
    var killSwitch by remember { mutableStateOf(true) }
    SettingsGroup("WireGuard VPN") {
        SettingsToggleRow(Icons.Default.VpnKey, BentoColors.success, "VPN", if (vpnEnabled) "Connected" else "Disconnected", checked = vpnEnabled, onCheckedChange = { vpnEnabled = it })
        SettingsToggleRow(Icons.Default.Block, BentoColors.error, "Kill Switch", "Block internet if VPN drops", checked = killSwitch, onCheckedChange = { killSwitch = it }, showDivider = false)
    }
    SettingsGroup("Configuration") { SettingsNavRow(Icons.Default.Add, BentoColors.accent, "Import WireGuard Config", showDivider = false) }
}

@Composable fun DisplaySettings() = SettingsPage("Display & HDR") {
    var hdr by remember { mutableStateOf(true) }
    var cec by remember { mutableStateOf(true) }
    var atmos by remember { mutableStateOf(false) }
    var forceGpu by remember { mutableStateOf(true) }
    SettingsGroup("Output") {
        SettingsToggleRow(Icons.Default.Hdr, Color(0xFFFFCC00), "HDR10 Tone Mapping", checked = hdr, onCheckedChange = { hdr = it })
        SettingsToggleRow(Icons.Default.Tv, BentoColors.accent, "HDMI CEC", "Control TV with remote", checked = cec, onCheckedChange = { cec = it }, showDivider = false)
    }
    SettingsGroup("Audio") { SettingsToggleRow(Icons.Default.SurroundSound, Color(0xFF007AFF), "Dolby Atmos Passthrough", checked = atmos, onCheckedChange = { atmos = it }, showDivider = false) }
    SettingsGroup("Performance") { SettingsToggleRow(Icons.Default.Speed, BentoColors.success, "Force GPU Rendering", checked = forceGpu, onCheckedChange = { forceGpu = it }, showDivider = false) }
}

@Composable fun SoundSettings() = SettingsPage("Sound") {
    var volume by remember { mutableStateOf(0.7f) }
    SettingsGroup("Volume") {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("System Volume", fontFamily = InterFamily, fontSize = 14.sp, color = BentoColors.textPrimary)
            Slider(value = volume, onValueChange = { volume = it },
                colors = SliderDefaults.colors(thumbColor = BentoColors.accent,
                    activeTrackColor = BentoColors.accent, inactiveTrackColor = BentoColors.surfaceElevated))
        }
    }
}

@Composable fun PrivacySettings() = SettingsPage("Privacy & Safety") {
    var micIndicator by remember { mutableStateOf(true) }
    var cameraIndicator by remember { mutableStateOf(true) }
    var macRandom by remember { mutableStateOf(true) }
    var locationIndicator by remember { mutableStateOf(true) }
    SettingsGroup("Indicators") {
        SettingsToggleRow(Icons.Default.Mic, Color(0xFFFF3B30), "Microphone Indicator", "Always visible in menubar", checked = micIndicator, onCheckedChange = { micIndicator = it })
        SettingsToggleRow(Icons.Default.Videocam, Color(0xFFFF9500), "Camera Indicator", "Always visible in menubar", checked = cameraIndicator, onCheckedChange = { cameraIndicator = it })
        SettingsToggleRow(Icons.Default.LocationOn, BentoColors.accent, "Location Indicator", checked = locationIndicator, onCheckedChange = { locationIndicator = it }, showDivider = false)
    }
    SettingsGroup("Network Privacy") { SettingsToggleRow(Icons.Default.Shuffle, BentoColors.success, "MAC Address Randomization", "Randomize per network", checked = macRandom, onCheckedChange = { macRandom = it }, showDivider = false) }
    SettingsGroup("App Permissions") { SettingsNavRow(Icons.Default.Apps, BentoColors.accent, "Per-App Network Access", showDivider = false) }
}

@Composable fun GamingSettings() = SettingsPage("Gaming Mode") {
    var gamingMode by remember { mutableStateOf(false) }
    var fpsOverlay by remember { mutableStateOf(true) }
    var recording by remember { mutableStateOf(false) }
    var cpuBoost by remember { mutableStateOf(true) }
    val gamingGreen = Color(0xFF00FF88)
    SettingsGroup { SettingsToggleRow(Icons.Default.SportsEsports, gamingGreen, "Gaming Mode", "Boost performance, disable notifications", checked = gamingMode, onCheckedChange = { gamingMode = it }, showDivider = false) }
    SettingsGroup("Overlay") {
        SettingsToggleRow(Icons.Default.Speed, gamingGreen, "FPS Overlay", checked = fpsOverlay, onCheckedChange = { fpsOverlay = it })
        SettingsToggleRow(Icons.Default.FiberManualRecord, Color(0xFFFF3B30), "Game Recording", checked = recording, onCheckedChange = { recording = it }, showDivider = false)
    }
    SettingsGroup("Performance") { SettingsToggleRow(Icons.Default.FlashOn, gamingGreen, "CPU Governor Boost", "Performance mode during gaming", checked = cpuBoost, onCheckedChange = { cpuBoost = it }, showDivider = false) }
    SettingsGroup("Gamepad") { SettingsNavRow(Icons.Default.Gamepad, BentoColors.textSecondary, "Gamepad Mapper", showDivider = false) }
}

@Composable fun DnsSettings() = SettingsPage("DNS Ad Blocker") {
    var dnsEnabled by remember { mutableStateOf(true) }
    var blockTrackers by remember { mutableStateOf(true) }
    var blockAds by remember { mutableStateOf(true) }
    var blockMalware by remember { mutableStateOf(true) }
    SettingsGroup { SettingsToggleRow(Icons.Default.Block, BentoColors.accent, "DNS Ad Blocker", "Pi-hole built-in", checked = dnsEnabled, onCheckedChange = { dnsEnabled = it }, showDivider = false) }
    if (dnsEnabled) {
        SettingsGroup("Block Lists") {
            SettingsToggleRow(label = "Ads", checked = blockAds, onCheckedChange = { blockAds = it })
            SettingsToggleRow(label = "Trackers", checked = blockTrackers, onCheckedChange = { blockTrackers = it })
            SettingsToggleRow(label = "Malware domains", checked = blockMalware, onCheckedChange = { blockMalware = it }, showDivider = false)
        }
        SettingsGroup("Statistics") { SettingsNavRow(Icons.Default.BarChart, BentoColors.accent, "View Blocked Requests", showDivider = false) }
    }
}

@Composable fun DesktopSettings() = SettingsPage("Desktop & Dock") {
    var hotCorners by remember { mutableStateOf(true) }
    var dockMagnify by remember { mutableStateOf(true) }
    var dockAutohide by remember { mutableStateOf(false) }
    var missionControl by remember { mutableStateOf(true) }
    SettingsGroup("Dock") {
        SettingsToggleRow(Icons.Default.AspectRatio, BentoColors.accent, "Magnification", "Enlarge icons on hover", checked = dockMagnify, onCheckedChange = { dockMagnify = it })
        SettingsToggleRow(Icons.Default.VisibilityOff, BentoColors.textSecondary, "Auto-Hide Dock", checked = dockAutohide, onCheckedChange = { dockAutohide = it }, showDivider = false)
    }
    SettingsGroup("Desktop") {
        SettingsToggleRow(Icons.Default.Crop, BentoColors.accent, "Hot Corners", checked = hotCorners, onCheckedChange = { hotCorners = it })
        SettingsToggleRow(Icons.Default.GridView, BentoColors.accent, "Mission Control Gesture", checked = missionControl, onCheckedChange = { missionControl = it }, showDivider = false)
    }
    SettingsGroup("Virtual Desktops") { SettingsNavRow(Icons.Default.Add, BentoColors.accent, "Manage Virtual Desktops", showDivider = false) }
}

@Composable fun AppearanceSettings() = SettingsPage("Appearance") {
    val accentColors = listOf(Color(0xFF6C6CFF) to "Indigo", Color(0xFF007AFF) to "Blue",
        Color(0xFF34C759) to "Green", Color(0xFFFF9500) to "Orange",
        Color(0xFFFF3B30) to "Red", Color(0xFFAF52DE) to "Purple")
    var selectedAccent by remember { mutableStateOf(0) }
    SettingsGroup("Accent Color") {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            accentColors.forEachIndexed { i, (color, _) ->
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(color).clickable { selectedAccent = i }
                    .border(width = if (selectedAccent == i) 2.dp else 0.dp, color = Color.White, shape = CircleShape))
            }
        }
    }
    SettingsGroup("Fonts") { SettingsNavRow(Icons.Default.TextFields, BentoColors.accent, "System Font", "Inter", showDivider = false) }
    SettingsGroup("Wallpaper") { SettingsNavRow(Icons.Default.Wallpaper, BentoColors.accent, "Change Wallpaper", showDivider = false) }
}

@Composable fun AppsSettings() = SettingsPage("Apps") {
    SettingsGroup {
        SettingsNavRow(Icons.Default.ShoppingCart, BentoColors.success, "Play Store", "Sandboxed Google Play", showDivider = true)
        SettingsNavRow(Icons.Default.Apps, BentoColors.accent, "All Apps", showDivider = true)
        SettingsNavRow(Icons.Default.Storage, BentoColors.textSecondary, "App Permissions", showDivider = false)
    }
}

@Composable fun StorageSettings() = SettingsPage("Storage") {
    SettingsGroup("Internal Storage") {
        Column(modifier = Modifier.padding(16.dp)) {
            LinearProgressIndicator(progress = 0.35f, color = BentoColors.accent,
                trackColor = BentoColors.surfaceElevated,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Text("5.6 GB used of 16 GB", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textSecondary)
        }
    }
    SettingsGroup("Tools") { SettingsNavRow(Icons.Default.FolderOpen, BentoColors.accent, "Open File Manager", showDivider = false) }
}

@Composable fun UpdateSettings() = SettingsPage("System Updates") {
    SettingsGroup {
        SettingsNavRow(Icons.Default.SystemUpdate, BentoColors.accent, "Check for Updates", badge = "1", showDivider = true)
        SettingsNavRow(Icons.Default.History, BentoColors.textSecondary, "Update History", showDivider = false)
    }
    SettingsGroup("Update Channel") {
        SettingsNavRow(label = "Stable", subtitle = "Recommended", showDivider = true)
        SettingsNavRow(label = "Beta", showDivider = false)
    }
}

@Composable fun DeveloperSettings() = SettingsPage("Developer") {
    var adb by remember { mutableStateOf(false) }
    var adbWifi by remember { mutableStateOf(false) }
    var showLayout by remember { mutableStateOf(false) }
    SettingsGroup("ADB") {
        SettingsToggleRow(Icons.Default.Usb, BentoColors.accent, "USB Debugging", checked = adb, onCheckedChange = { adb = it })
        SettingsToggleRow(Icons.Default.Wifi, BentoColors.accent, "Wireless ADB", checked = adbWifi, onCheckedChange = { adbWifi = it }, showDivider = false)
    }
    SettingsGroup("Debug") { SettingsToggleRow(Icons.Default.GridOn, BentoColors.textSecondary, "Show Layout Bounds", checked = showLayout, onCheckedChange = { showLayout = it }, showDivider = false) }
    SettingsGroup("Terminal") { SettingsNavRow(Icons.Default.Terminal, BentoColors.success, "Open Termux", showDivider = false) }
}

@Composable fun AboutSettings() = SettingsPage("About BentoOS") {
    val rows = listOf("OS Name" to "BentoOS", "Version" to "1.0.0", "Build" to "bento-1.0.0-s905x",
        "Android Base" to "Android 16 AOSP", "Kernel" to "Linux 6.6 LTS",
        "Device" to "Amlogic S905X", "Architecture" to "arm64-v8a", "Created by" to "Ibtisam")
    SettingsGroup {
        rows.forEachIndexed { i, (label, value) ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontFamily = InterFamily, fontSize = 14.sp, color = BentoColors.textSecondary)
                Text(value, fontFamily = if (label in listOf("Version", "Build", "Kernel")) MonoFamily else InterFamily,
                    fontSize = 14.sp, color = BentoColors.textPrimary)
            }
            if (i < rows.size - 1) Divider(color = BentoColors.border, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
        }
    }
    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(BentoColors.textSecondary))
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(BentoColors.textSecondary))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(BentoColors.textSecondary))
                    Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(BentoColors.textSecondary))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("BentoOS", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = BentoColors.textSecondary)
            Text("Everything in its place.", fontFamily = InterFamily, fontSize = 12.sp, color = BentoColors.textSecondary.copy(alpha = 0.6f))
        }
    }
}
