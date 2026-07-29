package com.example.ui.editor

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecentFileEntity
import com.example.data.model.EditorTextSize
import com.example.data.model.EditorTheme
import com.example.ui.dialogs.*
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.TextEditorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val context = LocalContext.current

    TextEditorTheme(theme = uiState.theme) {
        val backgroundColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground
        val menuBgColor = MaterialTheme.colorScheme.surface

        val fontFamily = when (uiState.typeface.lowercase()) {
            "monospace", "courier", "courier new", "monaco" -> FontFamily.Monospace
            "serif", "georgia", "baskerville", "palatino", "goudy" -> FontFamily.Serif
            "cursive" -> FontFamily.Cursive
            "sans-serif-black" -> FontFamily.SansSerif
            "sans-serif-condensed-light" -> FontFamily.SansSerif
            else -> FontFamily.Default
        }

        val fontSizeSp = uiState.textSize.spSize.sp

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = backgroundColor,
            topBar = {
                if (uiState.isSearchMode) {
                    SearchTopBar(
                        searchQuery = uiState.searchQuery,
                        replaceQuery = uiState.replaceQuery,
                        matchCount = uiState.searchMatches.size,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onReplaceChange = { viewModel.setReplaceQuery(it) },
                        onReplaceCurrent = { viewModel.replaceCurrentMatch() },
                        onReplaceAll = { viewModel.replaceAllMatches() },
                        onCloseSearch = { viewModel.setSearchMode(false) }
                    )
                } else {
                    EditorTopBar(
                        fileName = uiState.fileName,
                        encoding = uiState.encoding,
                        cursorLine = uiState.cursorLine,
                        totalLines = uiState.totalLines,
                        isCloudSynced = uiState.isCloudSynced,
                        collaboratorCount = uiState.collaborators.size,
                        isMarkdownPreview = uiState.isMarkdownPreviewOpen,
                        onSaveClick = { viewModel.saveCurrentDocument() },
                        onMarkdownToggle = { viewModel.setMarkdownPreviewOpen(!uiState.isMarkdownPreviewOpen) },
                        onCollabClick = { viewModel.setCollabDialogOpen(true) },
                        onMenuClick = { viewModel.setMenuExpanded(true) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(backgroundColor)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Line numbers column
                    if (uiState.options.viewFiles) {
                        LineNumbersColumn(
                            totalLines = uiState.totalLines,
                            activeLine = uiState.cursorLine,
                            textColor = textColor,
                            fontSizeSp = fontSizeSp,
                            fontFamily = fontFamily
                        )
                    }

                    // Main Text Editor surface
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(8.dp)
                    ) {
                        if (uiState.isMarkdownPreviewOpen) {
                            MarkdownPreviewView(
                                content = uiState.content,
                                textColor = textColor,
                                fontSizeSp = fontSizeSp
                            )
                        } else {
                            BasicTextField(
                                value = uiState.content,
                                onValueChange = { newText ->
                                    viewModel.updateContent(newText)
                                },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = TextStyle(
                                    color = textColor,
                                    fontSize = fontSizeSp,
                                    fontFamily = fontFamily,
                                    lineHeight = (uiState.textSize.spSize + 6).sp
                                ),
                                cursorBrush = SolidColor(AccentCyan),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        if (uiState.content.isEmpty()) {
                                            Text(
                                                text = "Start typing...",
                                                color = Color.Gray,
                                                fontSize = fontSizeSp,
                                                fontFamily = fontFamily
                                            )
                                        }
                                        innerTextField()

                                        // Collaborators live typing indicator overlay
                                        uiState.collaborators.filter { it.isTyping }.forEach { collab ->
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .background(Color(collab.colorHex).copy(alpha = 0.85f), shape = RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(Color.White, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${collab.name} typing...",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Custom Overflow Dropdown Menu with Submenus
                if (uiState.isMenuExpanded) {
                    EditorDropdownMenus(
                        uiState = uiState,
                        recentFiles = recentFiles,
                        menuBgColor = menuBgColor,
                        onDismiss = { viewModel.setMenuExpanded(false) },
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }

        // Dialogs
        if (uiState.isSaveAsDialogOpen) {
            SaveFileDialog(
                initialPath = uiState.filePath,
                onDismiss = { viewModel.setSaveAsDialogOpen(false) },
                onSave = { newPath -> viewModel.saveAs(newPath) },
                onStorageClick = { viewModel.setStorageExplorerOpen(true) }
            )
        }

        if (uiState.isStorageExplorerOpen) {
            StorageExplorerDialog(
                currentDirectoryPath = uiState.currentStorageDirectory,
                onDismiss = { viewModel.setStorageExplorerOpen(false) },
                onFileSelected = { selectedPath ->
                    viewModel.openRecentFile(selectedPath)
                }
            )
        }

        if (uiState.isUnsavedChangesDialogOpen) {
            UnsavedChangesDialog(
                onDismiss = { viewModel.setUnsavedChangesDialogOpen(false) },
                onDiscard = { viewModel.discardUnsavedChanges() },
                onSave = { viewModel.saveAndProceedUnsavedChanges() }
            )
        }

        if (uiState.isGoToLineDialogOpen) {
            GoToLineDialog(
                currentLine = uiState.cursorLine,
                maxLines = uiState.totalLines,
                onDismiss = { viewModel.setGoToLineDialogOpen(false) },
                onGoToLine = { targetLine -> viewModel.goToLine(targetLine) }
            )
        }

        if (uiState.isAboutDialogOpen) {
            AboutDialog(onDismiss = { viewModel.setAboutDialogOpen(false) })
        }

        if (uiState.isCollabDialogOpen) {
            CollabRoomDialog(
                currentRoomId = uiState.collabRoomId,
                collaborators = uiState.collaborators,
                isCloudSynced = uiState.isCloudSynced,
                onDismiss = { viewModel.setCollabDialogOpen(false) },
                onJoinOrCreateRoom = { newRoom ->
                    viewModel.setCollabRoom(newRoom)
                    Toast.makeText(context, "Connected to collaboration room: $newRoom", Toast.LENGTH_SHORT).show()
                },
                onForceSync = {
                    viewModel.saveCurrentDocument()
                    Toast.makeText(context, "Synced document to cloud", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    fileName: String,
    encoding: String,
    cursorLine: Int,
    totalLines: Int,
    isCloudSynced: Boolean,
    collaboratorCount: Int,
    isMarkdownPreview: Boolean,
    onSaveClick: () -> Unit,
    onMarkdownToggle: () -> Unit,
    onCollabClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1B1B1E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = encoding,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            // Real-time Collab & Cloud Sync Badge
            Surface(
                onClick = onCollabClick,
                color = Color(0xFF2C2C2E),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Cloud Sync",
                        tint = if (isCloudSynced) Color(0xFF4CAF50) else Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$collaboratorCount Live",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "$cursorLine\n$totalLines",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            IconButton(onClick = onMarkdownToggle) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = "View markdown...",
                    tint = if (isMarkdownPreview) AccentCyan else Color.LightGray
                )
            }

            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = Color.LightGray
                )
            }

            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu options",
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    searchQuery: String,
    replaceQuery: String,
    matchCount: Int,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onCloseSearch: () -> Unit
) {
    Surface(
        color = Color(0xFF1B1B1E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    cursorBrush = SolidColor(AccentCyan),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text("Search...", color = Color.Gray, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    }
                )

                Text(
                    text = "$matchCount matches",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Replace options row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = replaceQuery,
                    onValueChange = onReplaceChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(AccentCyan),
                    decorationBox = { innerTextField ->
                        Box {
                            if (replaceQuery.isEmpty()) {
                                Text("Replace with...", color = Color.Gray, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )

                TextButton(onClick = onReplaceCurrent) {
                    Text("Replace", color = AccentCyan, fontSize = 12.sp)
                }

                TextButton(onClick = onReplaceAll) {
                    Text("All", color = AccentCyan, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LineNumbersColumn(
    totalLines: Int,
    activeLine: Int,
    textColor: Color,
    fontSizeSp: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(36.dp)
            .background(Color(0xFF141416))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.End
    ) {
        for (i in 1..maxOf(1, totalLines)) {
            Text(
                text = "$i",
                color = if (i == activeLine) AccentCyan else Color.DarkGray,
                fontSize = fontSizeSp,
                fontFamily = fontFamily,
                fontWeight = if (i == activeLine) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}

@Composable
private fun MarkdownPreviewView(
    content: String,
    textColor: Color,
    fontSizeSp: androidx.compose.ui.unit.TextUnit
) {
    val annotatedString = buildAnnotatedString {
        val lines = content.split("\n")
        lines.forEach { line ->
            when {
                line.startsWith("# ") -> {
                    withStyle(SpanStyle(color = AccentCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)) {
                        append(line.substringAfter("# ") + "\n")
                    }
                }
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(color = Color(0xFF64B5F6), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)) {
                        append(line.substringAfter("## ") + "\n")
                    }
                }
                line.startsWith("//") -> {
                    withStyle(SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic)) {
                        append(line + "\n")
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = textColor, fontSize = fontSizeSp)) {
                        append(line + "\n")
                    }
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun EditorDropdownMenus(
    uiState: EditorUiState,
    recentFiles: List<RecentFileEntity>,
    menuBgColor: Color,
    onDismiss: () -> Unit,
    viewModel: EditorViewModel,
    context: android.content.Context
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 12.dp)
                .width(260.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF232325)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                // Check if any sub-menu is active
                when {
                    uiState.isRecentSubmenuExpanded -> SubmenuOpenRecent(
                        recentFiles = recentFiles,
                        onFileClick = { path ->
                            viewModel.openRecentFile(path)
                            viewModel.setRecentSubmenuExpanded(false)
                            onDismiss()
                        },
                        onClearClick = {
                            viewModel.clearRecentFiles()
                            viewModel.setRecentSubmenuExpanded(false)
                        }
                    )

                    uiState.isTypefaceSubmenuExpanded -> SubmenuTypeface(
                        selected = uiState.typeface,
                        onSelect = { typeface -> viewModel.setTypeface(typeface) }
                    )

                    uiState.isTextSizeSubmenuExpanded -> SubmenuTextSize(
                        selected = uiState.textSize,
                        onSelect = { size -> viewModel.setTextSize(size) }
                    )

                    uiState.isThemeSubmenuExpanded -> SubmenuTheme(
                        selected = uiState.theme,
                        onSelect = { theme -> viewModel.setTheme(theme) }
                    )

                    uiState.isOptionsSubmenuExpanded -> SubmenuOptions(
                        options = uiState.options,
                        viewModel = viewModel
                    )

                    uiState.isEncodingSubmenuExpanded -> SubmenuEncoding(
                        selectedEncoding = uiState.encoding,
                        onSelect = { enc -> viewModel.setEncoding(enc) }
                    )

                    else -> MainMenuItemsList(
                        onNewFile = {
                            viewModel.handleNewFile()
                            onDismiss()
                        },
                        onOpenFile = {
                            viewModel.handleOpenFileRequest()
                            onDismiss()
                        },
                        onOpenRecent = { viewModel.setRecentSubmenuExpanded(true) },
                        onSearch = {
                            viewModel.setSearchMode(true)
                            onDismiss()
                        },
                        onSaveAs = {
                            viewModel.setSaveAsDialogOpen(true)
                            onDismiss()
                        },
                        onGoTo = {
                            viewModel.setGoToLineDialogOpen(true)
                            onDismiss()
                        },
                        onPrint = {
                            Toast.makeText(context, "Printing document...", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        onViewMarkdown = {
                            viewModel.setMarkdownPreviewOpen(!uiState.isMarkdownPreviewOpen)
                            onDismiss()
                        },
                        onEncoding = { viewModel.setEncodingSubmenuExpanded(true) },
                        onOptions = { viewModel.setOptionsSubmenuExpanded(true) },
                        onTheme = { viewModel.setThemeSubmenuExpanded(true) },
                        onTextSize = { viewModel.setTextSizeSubmenuExpanded(true) },
                        onTypeface = { viewModel.setTypefaceSubmenuExpanded(true) },
                        onAbout = {
                            viewModel.setAboutDialogOpen(true)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMenuItemsList(
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit,
    onOpenRecent: () -> Unit,
    onSearch: () -> Unit,
    onSaveAs: () -> Unit,
    onGoTo: () -> Unit,
    onPrint: () -> Unit,
    onViewMarkdown: () -> Unit,
    onEncoding: () -> Unit,
    onOptions: () -> Unit,
    onTheme: () -> Unit,
    onTextSize: () -> Unit,
    onTypeface: () -> Unit,
    onAbout: () -> Unit
) {
    MenuItemRow(label = "New file", onClick = onNewFile)
    MenuItemRow(label = "Open file...", onClick = onOpenFile)
    MenuItemRow(label = "Open recent", hasArrow = true, onClick = onOpenRecent)
    MenuItemRow(label = "Search...", onClick = onSearch)
    MenuItemRow(label = "Save as...", onClick = onSaveAs)
    MenuItemRow(label = "Go to...", onClick = onGoTo)
    MenuItemRow(label = "Print...", onClick = onPrint)
    MenuItemRow(label = "View markdown...", onClick = onViewMarkdown)
    MenuItemRow(label = "UTF-8", hasArrow = true, onClick = onEncoding)
    MenuItemRow(label = "Options", hasArrow = true, onClick = onOptions)
    MenuItemRow(label = "Theme", hasArrow = true, onClick = onTheme)
    MenuItemRow(label = "Text size", hasArrow = true, onClick = onTextSize)
    MenuItemRow(label = "Typeface", hasArrow = true, onClick = onTypeface)
    MenuItemRow(label = "About", onClick = onAbout)
}

@Composable
private fun MenuItemRow(
    label: String,
    hasArrow: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp
        )
        if (hasArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Submenu Image 1 - Open recent
@Composable
private fun SubmenuOpenRecent(
    recentFiles: List<RecentFileEntity>,
    onFileClick: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Open recent",
            color = AccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        recentFiles.forEach { file ->
            Text(
                text = file.path,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFileClick(file.path) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
        Text(
            text = "Clear list",
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClearClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

// Submenu Image 7 - Typeface
@Composable
private fun SubmenuTypeface(
    selected: String,
    onSelect: (String) -> Unit
) {
    val typefaces = listOf(
        "arial", "baskerville", "casual", "courier new", "courier",
        "cursive", "fantasy", "georgia", "goudy", "helvetica",
        "monaco", "monospace", "palatino", "sans-serif-black", "sans-serif-condensed-light"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Typeface",
            color = AccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(typefaces) { tf ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(tf) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tf,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Checkbox(
                        checked = selected.equals(tf, ignoreCase = true),
                        onCheckedChange = { onSelect(tf) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentCyan,
                            checkmarkColor = Color.Black
                        )
                    )
                }
            }
        }
    }
}

// Submenu Image 8 - Text size
@Composable
private fun SubmenuTextSize(
    selected: EditorTextSize,
    onSelect: (EditorTextSize) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Text size",
            color = AccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        EditorTextSize.values().forEach { size ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(size) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = size.label,
                    color = Color.White,
                    fontSize = 15.sp
                )
                RadioButton(
                    selected = (selected == size),
                    onClick = { onSelect(size) },
                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                )
            }
        }
    }
}

// Submenu Image 9 - Theme
@Composable
private fun SubmenuTheme(
    selected: EditorTheme,
    onSelect: (EditorTheme) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Theme",
            color = AccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        EditorTheme.values().forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(theme) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme.label,
                    color = Color.White,
                    fontSize = 15.sp
                )
                RadioButton(
                    selected = (selected == theme),
                    onClick = { onSelect(theme) },
                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                )
            }
        }
    }
}

// Submenu Image 10 - Options
@Composable
private fun SubmenuOptions(
    options: com.example.data.model.EditorOptions,
    viewModel: EditorViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Options",
            color = AccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        OptionCheckboxRow("View files", options.viewFiles) { viewModel.toggleOptionViewFiles() }
        OptionCheckboxRow("Open last", options.openLast) { viewModel.toggleOptionOpenLast() }
        OptionCheckboxRow("Auto save", options.autoSave) { viewModel.toggleOptionAutoSave() }
        OptionCheckboxRow("Word wrap", options.wordWrap) { viewModel.toggleOptionWordWrap() }
        OptionCheckboxRow("Suggestions", options.suggestions) { viewModel.toggleOptionSuggestions() }
        OptionCheckboxRow("Highlight syntax", options.highlightSyntax) { viewModel.toggleOptionHighlightSyntax() }
    }
}

@Composable
private fun OptionCheckboxRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 15.sp
        )
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = AccentCyan,
                checkmarkColor = Color.Black
            )
        )
    }
}

// Submenu Image 11 - UTF-8 / Encodings
@Composable
private fun SubmenuEncoding(
    selectedEncoding: String,
    onSelect: (String) -> Unit
) {
    val encodings = listOf(
        "UTF-8", "Detect", "Adobe-Standard-Encoding", "Big5", "Big5-HKSCS",
        "BOCU-1", "CESU-8", "cp1363", "cp851", "EUC-JP", "EUC-KR",
        "GB18030", "GBK", "hp-roman8", "HZ-GB-2312", "IBM-Thai", "ASCII", "UTF-16"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = selectedEncoding,
            color = AccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(encodings) { enc ->
                Text(
                    text = enc,
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(enc) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
