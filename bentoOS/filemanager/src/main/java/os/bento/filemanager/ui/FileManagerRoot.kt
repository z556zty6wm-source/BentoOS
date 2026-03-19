package os.bento.filemanager.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.bento.settings.ui.theme.BentoColors
import os.bento.settings.ui.theme.InterFamily
import os.bento.settings.ui.theme.MonoFamily

enum class FileType { FOLDER, IMAGE, VIDEO, AUDIO, DOCUMENT, APK, ARCHIVE, CODE, OTHER }
enum class ViewMode { GRID, LIST }
enum class SortMode { NAME, DATE, SIZE, TYPE }

data class FileItem(
    val name: String, val type: FileType,
    val size: String = "", val modified: String = "",
    val path: String = "", val isHidden: Boolean = false,
)

data class SidebarLocation(
    val id: String, val icon: ImageVector, val label: String,
    val path: String, val tint: Color = BentoColors.accent, val isRemote: Boolean = false,
)

val sampleFiles = listOf(
    FileItem("Documents", FileType.FOLDER, modified = "Today"),
    FileItem("Downloads", FileType.FOLDER, modified = "Today"),
    FileItem("Pictures", FileType.FOLDER, modified = "Yesterday"),
    FileItem("Videos", FileType.FOLDER, modified = "3 days ago"),
    FileItem("Music", FileType.FOLDER, modified = "1 week ago"),
    FileItem("BentoOS_v1.0.apk", FileType.APK, "48.2 MB", "Today"),
    FileItem("wallpaper.jpg", FileType.IMAGE, "3.4 MB", "Yesterday"),
    FileItem("recording.mp4", FileType.VIDEO, "214 MB", "3 days ago"),
    FileItem("notes.md", FileType.DOCUMENT, "12 KB", "Today"),
    FileItem("BentoOS.zip", FileType.ARCHIVE, "82 MB", "1 week ago"),
    FileItem("build.gradle", FileType.CODE, "4 KB", "2 days ago"),
    FileItem("song.flac", FileType.AUDIO, "28 MB", "1 week ago"),
)

val sidebarLocations = listOf(
    SidebarLocation("home",      Icons.Default.Home,         "Home",         "/sdcard"),
    SidebarLocation("downloads", Icons.Default.Download,     "Downloads",    "/sdcard/Download"),
    SidebarLocation("documents", Icons.Default.Description,  "Documents",    "/sdcard/Documents", Color(0xFF007AFF)),
    SidebarLocation("pictures",  Icons.Default.Photo,        "Pictures",     "/sdcard/Pictures",  Color(0xFFFF9500)),
    SidebarLocation("videos",    Icons.Default.VideoLibrary, "Videos",       "/sdcard/Movies",    Color(0xFFFF3B30)),
    SidebarLocation("music",     Icons.Default.MusicNote,    "Music",        "/sdcard/Music",     Color(0xFF34C759)),
    SidebarLocation("root",      Icons.Default.Storage,      "Internal",     "/sdcard",           BentoColors.textSecondary),
    SidebarLocation("external",  Icons.Default.SdCard,       "SD Card",      "/mnt/sdcard1",      BentoColors.textSecondary),
    SidebarLocation("smb",       Icons.Default.Computer,     "Network (SMB)","smb://",            BentoColors.accent, true),
    SidebarLocation("nfs",       Icons.Default.Dns,          "Network (NFS)","nfs://",            BentoColors.accent, true),
    SidebarLocation("ftp",       Icons.Default.CloudQueue,   "FTP Server",   "ftp://",            BentoColors.accent, true),
)

@Composable
fun FileManagerRoot() {
    var selectedLocation by remember { mutableStateOf("home") }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    val breadcrumbs = remember { mutableStateListOf("Home") }

    Row(modifier = Modifier.fillMaxSize().background(BentoColors.bg)) {
        FileManagerSidebar(locations = sidebarLocations, selectedId = selectedLocation,
            onSelect = {
                selectedLocation = it
                breadcrumbs.clear()
                breadcrumbs.add(sidebarLocations.first { l -> l.id == it }.label)
                selectedFile = null
            })
        Box(Modifier.fillMaxHeight().width(0.5.dp).background(BentoColors.border))
        Column(modifier = Modifier.fillMaxSize()) {
            FileManagerToolbar(breadcrumbs = breadcrumbs, viewMode = viewMode,
                searchQuery = searchQuery, onViewModeChange = { viewMode = it },
                onSearchChange = { searchQuery = it },
                onNavigateUp = { if (breadcrumbs.size > 1) breadcrumbs.removeLast() })
            Divider(color = BentoColors.border, thickness = 0.5.dp)
            val displayFiles = sampleFiles.filter { searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) }
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (viewMode) {
                        ViewMode.GRID -> FileGrid(displayFiles, selectedFile,
                            onFileClick = { selectedFile = it },
                            onFolderOpen = { breadcrumbs.add(it.name); selectedFile = null })
                        ViewMode.LIST -> FileList(displayFiles, selectedFile,
                            onFileClick = { selectedFile = it },
                            onFolderOpen = { breadcrumbs.add(it.name); selectedFile = null })
                    }
                }
                if (selectedFile != null) {
                    Box(Modifier.fillMaxHeight().width(0.5.dp).background(BentoColors.border))
                    FileInfoPanel(file = selectedFile!!, onDismiss = { selectedFile = null })
                }
            }
        }
    }
}

@Composable
fun FileManagerSidebar(locations: List<SidebarLocation>, selectedId: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.width(200.dp).fillMaxHeight()
        .background(BentoColors.surface).padding(vertical = 12.dp)) {
        Text("Finder", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp, color = BentoColors.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
        SidebarSection("Locations", locations.filter { !it.isRemote }, selectedId, onSelect)
        SidebarSection("Network", locations.filter { it.isRemote }, selectedId, onSelect)
    }
}

@Composable
fun SidebarSection(title: String, locations: List<SidebarLocation>, selectedId: String, onSelect: (String) -> Unit) {
    Text(title.uppercase(), fontFamily = InterFamily, fontSize = 10.sp, letterSpacing = 0.8.sp,
        color = BentoColors.textSecondary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
    locations.forEach { loc ->
        val isSelected = loc.id == selectedId
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) BentoColors.accentDim else Color.Transparent)
            .clickable { onSelect(loc.id) }.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(loc.icon, null, tint = loc.tint, modifier = Modifier.size(14.dp))
            Text(loc.label, fontFamily = InterFamily, fontSize = 13.sp,
                color = if (isSelected) BentoColors.textPrimary else BentoColors.textSecondary)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun FileManagerToolbar(
    breadcrumbs: List<String>, viewMode: ViewMode, searchQuery: String,
    onViewModeChange: (ViewMode) -> Unit, onSearchChange: (String) -> Unit, onNavigateUp: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().background(BentoColors.surface)
        .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onNavigateUp, enabled = breadcrumbs.size > 1) {
            Icon(Icons.Default.ArrowBack, "Back",
                tint = if (breadcrumbs.size > 1) BentoColors.textPrimary else BentoColors.textSecondary,
                modifier = Modifier.size(18.dp))
        }
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            breadcrumbs.forEachIndexed { index, crumb ->
                if (index > 0) Icon(Icons.Default.ChevronRight, null, tint = BentoColors.textSecondary, modifier = Modifier.size(14.dp))
                Text(crumb, fontFamily = InterFamily, fontSize = 13.sp,
                    color = if (index == breadcrumbs.lastIndex) BentoColors.textPrimary else BentoColors.textSecondary,
                    fontWeight = if (index == breadcrumbs.lastIndex) FontWeight.Medium else FontWeight.Normal)
            }
        }
        OutlinedTextField(value = searchQuery, onValueChange = onSearchChange,
            placeholder = { Text("Search", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = BentoColors.textSecondary, modifier = Modifier.size(16.dp)) },
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoColors.accent,
                unfocusedBorderColor = BentoColors.border, focusedContainerColor = BentoColors.surfaceElevated,
                unfocusedContainerColor = BentoColors.surfaceElevated, focusedTextColor = BentoColors.textPrimary,
                unfocusedTextColor = BentoColors.textPrimary),
            modifier = Modifier.width(200.dp).height(40.dp))
        Row(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(BentoColors.surfaceElevated)
            .border(0.5.dp, BentoColors.border, RoundedCornerShape(6.dp))) {
            listOf(ViewMode.GRID to Icons.Default.GridView, ViewMode.LIST to Icons.Default.ViewList).forEach { (mode, icon) ->
                IconButton(onClick = { onViewModeChange(mode) }, modifier = Modifier.size(32.dp)
                    .background(if (viewMode == mode) BentoColors.accentDim else Color.Transparent)) {
                    Icon(icon, mode.name, tint = if (viewMode == mode) BentoColors.accent else BentoColors.textSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.CreateNewFolder, "New Folder", tint = BentoColors.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

fun fileIcon(type: FileType): ImageVector = when (type) {
    FileType.FOLDER   -> Icons.Default.Folder
    FileType.IMAGE    -> Icons.Default.Image
    FileType.VIDEO    -> Icons.Default.VideoFile
    FileType.AUDIO    -> Icons.Default.AudioFile
    FileType.DOCUMENT -> Icons.Default.Description
    FileType.APK      -> Icons.Default.Android
    FileType.ARCHIVE  -> Icons.Default.FolderZip
    FileType.CODE     -> Icons.Default.Code
    FileType.OTHER    -> Icons.Default.InsertDriveFile
}

fun fileIconColor(type: FileType): Color = when (type) {
    FileType.FOLDER   -> Color(0xFFFFCC00)
    FileType.IMAGE    -> Color(0xFFFF9500)
    FileType.VIDEO    -> Color(0xFFFF3B30)
    FileType.AUDIO    -> Color(0xFF34C759)
    FileType.DOCUMENT -> Color(0xFF007AFF)
    FileType.APK      -> Color(0xFF34C759)
    FileType.ARCHIVE  -> Color(0xFFAF52DE)
    FileType.CODE     -> BentoColors.accent
    FileType.OTHER    -> BentoColors.textSecondary
}

@Composable
fun FileGrid(files: List<FileItem>, selectedFile: FileItem?, onFileClick: (FileItem) -> Unit, onFolderOpen: (FileItem) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()) {
        items(files) { file ->
            val isSelected = selectedFile == file
            Column(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BentoColors.accentDim else Color.Transparent)
                .clickable { if (file.type == FileType.FOLDER) onFolderOpen(file) else onFileClick(file) }
                .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                    .background(fileIconColor(file.type).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    Icon(imageVector = fileIcon(file.type), contentDescription = file.name,
                        tint = fileIconColor(file.type), modifier = Modifier.size(28.dp))
                }
                Text(file.name, fontFamily = InterFamily, fontSize = 11.sp, color = BentoColors.textPrimary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
fun FileList(files: List<FileItem>, selectedFile: FileItem?, onFileClick: (FileItem) -> Unit, onFolderOpen: (FileItem) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().background(BentoColors.surface).padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text("Name", fontFamily = InterFamily, fontSize = 11.sp, color = BentoColors.textSecondary, modifier = Modifier.weight(1f))
            Text("Size", fontFamily = InterFamily, fontSize = 11.sp, color = BentoColors.textSecondary, modifier = Modifier.width(80.dp))
            Text("Modified", fontFamily = InterFamily, fontSize = 11.sp, color = BentoColors.textSecondary, modifier = Modifier.width(100.dp))
        }
        Divider(color = BentoColors.border, thickness = 0.5.dp)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(files) { file ->
                val isSelected = selectedFile == file
                Row(modifier = Modifier.fillMaxWidth()
                    .background(if (isSelected) BentoColors.accentDim else Color.Transparent)
                    .clickable { if (file.type == FileType.FOLDER) onFolderOpen(file) else onFileClick(file) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(fileIcon(file.type), null, tint = fileIconColor(file.type), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(file.name, fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textPrimary,
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(file.size, fontFamily = MonoFamily, fontSize = 12.sp, color = BentoColors.textSecondary, modifier = Modifier.width(80.dp))
                    Text(file.modified, fontFamily = InterFamily, fontSize = 12.sp, color = BentoColors.textSecondary, modifier = Modifier.width(100.dp))
                }
                Divider(color = BentoColors.border, thickness = 0.5.dp, modifier = Modifier.padding(start = 44.dp))
            }
        }
    }
}

@Composable
fun FileInfoPanel(file: FileItem, onDismiss: () -> Unit) {
    Column(modifier = Modifier.width(220.dp).fillMaxHeight()
        .background(BentoColors.surface).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Info", fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = BentoColors.textPrimary)
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "Close", tint = BentoColors.textSecondary, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp))
            .background(fileIconColor(file.type).copy(alpha = 0.15f)).align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center) {
            Icon(fileIcon(file.type), null, tint = fileIconColor(file.type), modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(file.name, fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp,
            color = BentoColors.textPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Divider(color = BentoColors.border, thickness = 0.5.dp)
        Spacer(Modifier.height(12.dp))
        val details = buildList {
            add("Kind" to file.type.name.lowercase().replaceFirstChar { it.uppercase() })
            if (file.size.isNotEmpty()) add("Size" to file.size)
            if (file.modified.isNotEmpty()) add("Modified" to file.modified)
            if (file.path.isNotEmpty()) add("Path" to file.path)
        }
        details.forEach { (key, value) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(key, fontFamily = InterFamily, fontSize = 12.sp, color = BentoColors.textSecondary)
                Text(value, fontFamily = InterFamily, fontSize = 12.sp, color = BentoColors.textPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.weight(1f))
        if (file.type != FileType.FOLDER) {
            Button(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoColors.accent)) {
                Text("Open", fontFamily = InterFamily, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.5.dp, BentoColors.border)) {
            Text("Share", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.textPrimary)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.5.dp, BentoColors.error.copy(alpha = 0.4f))) {
            Text("Delete", fontFamily = InterFamily, fontSize = 13.sp, color = BentoColors.error)
        }
    }
}
