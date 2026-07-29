package com.example.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.data.local.RecentFileEntity
import com.example.data.model.Collaborator
import com.example.data.model.EditorOptions
import com.example.data.model.EditorTextSize
import com.example.data.model.EditorTheme
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class EditorUiState(
    val fileName: String = "Editor.txt",
    val filePath: String = "/storage/emulated/99/Documents/Editor.txt",
    val content: String = "ygggg",
    val encoding: String = "UTF-8",
    val isModified: Boolean = false,
    val cursorLine: Int = 1,
    val cursorColumn: Int = 5,
    val totalLines: Int = 1,
    
    // Formatting & Preference Options
    val theme: EditorTheme = EditorTheme.BLACK,
    val textSize: EditorTextSize = EditorTextSize.MEDIUM,
    val typeface: String = "monospace",
    val options: EditorOptions = EditorOptions(
        viewFiles = true,
        openLast = false,
        autoSave = false,
        wordWrap = false,
        suggestions = true,
        highlightSyntax = true
    ),

    // Real-Time Collaboration & Cloud Sync State
    val isCloudSynced: Boolean = true,
    val collabRoomId: String? = "ROOM-8942",
    val isCollabActive: Boolean = true,
    val collaborators: List<Collaborator> = listOf(
        Collaborator("c1", "Sarah M.", 0xFF4CAF50, activeLine = 1, activeColumn = 3, isTyping = true),
        Collaborator("c2", "Alex K.", 0xFF2196F3, activeLine = 1, activeColumn = 6, isTyping = false)
    ),
    val liveSyncStatusMessage: String = "Synced to Cloud",

    // UI Overlay / Dialog Visibilities
    val isMenuExpanded: Boolean = false,
    val isRecentSubmenuExpanded: Boolean = false,
    val isEncodingSubmenuExpanded: Boolean = false,
    val isOptionsSubmenuExpanded: Boolean = false,
    val isThemeSubmenuExpanded: Boolean = false,
    val isTextSizeSubmenuExpanded: Boolean = false,
    val isTypefaceSubmenuExpanded: Boolean = false,

    val isSearchMode: Boolean = false,
    val isSaveAsDialogOpen: Boolean = false,
    val isStorageExplorerOpen: Boolean = false,
    val isUnsavedChangesDialogOpen: Boolean = false,
    val isGoToLineDialogOpen: Boolean = false,
    val isAboutDialogOpen: Boolean = false,
    val isCollabDialogOpen: Boolean = false,
    val isMarkdownPreviewOpen: Boolean = false,

    // Search Mode State
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val searchMatches: List<Int> = emptyList(),
    val currentMatchIndex: Int = 0,

    // Storage Picker Path & Breadcrumbs
    val currentStorageDirectory: String = "/storage/emulated/99/Documents",
    val pendingActionAfterUnsaved: (() -> Unit)? = null
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DocumentRepository(AppDatabase.getDatabase(application))

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val recentFiles: StateFlow<List<RecentFileEntity>> = repository.recentFiles.let { flow ->
        val stateFlow = MutableStateFlow<List<RecentFileEntity>>(emptyList())
        viewModelScope.launch {
            flow.collect { stateFlow.value = it }
        }
        stateFlow.asStateFlow()
    }

    private var autoSaveJob: Job? = null
    private var simulatedCollabJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            loadDocument("/storage/emulated/99/Documents/Editor.txt")
        }
        startSimulatedRealtimeCollaboration()
    }

    fun updateContent(newContent: String) {
        val lines = newContent.split("\n").size
        _uiState.update { state ->
            state.copy(
                content = newContent,
                isModified = true,
                totalLines = maxOf(1, lines),
                isCloudSynced = false,
                liveSyncStatusMessage = if (state.options.autoSave) "Auto-syncing..." else "Unsaved changes"
            )
        }

        if (_uiState.value.options.autoSave) {
            triggerAutoSave()
        }
    }

    fun updateCursorPosition(line: Int, column: Int) {
        _uiState.update { it.copy(cursorLine = line, cursorColumn = column) }
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1200)
            saveCurrentDocument()
        }
    }

    fun saveCurrentDocument() {
        viewModelScope.launch {
            val state = _uiState.value
            val doc = DocumentEntity(
                id = state.filePath,
                fileName = state.fileName,
                filePath = state.filePath,
                content = state.content,
                encoding = state.encoding,
                lastModified = System.currentTimeMillis(),
                isCloudSynced = true,
                cloudRoomId = state.collabRoomId
            )
            repository.saveDocument(doc)
            _uiState.update {
                it.copy(
                    isModified = false,
                    isCloudSynced = true,
                    liveSyncStatusMessage = "Cloud Synced (${System.currentTimeMillis().let { t -> 
                        java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(t)
                    }})"
                )
            }
        }
    }

    fun loadDocument(path: String) {
        viewModelScope.launch {
            val doc = repository.getDocument(path)
            if (doc != null) {
                _uiState.update { state ->
                    state.copy(
                        fileName = doc.fileName,
                        filePath = doc.filePath,
                        content = doc.content,
                        encoding = doc.encoding,
                        isModified = false,
                        totalLines = maxOf(1, doc.content.split("\n").size),
                        isCloudSynced = true,
                        liveSyncStatusMessage = "Synced from Cloud"
                    )
                }
            } else {
                // Create new or open placeholder
                val fileName = path.substringAfterLast("/")
                _uiState.update { state ->
                    state.copy(
                        fileName = fileName,
                        filePath = path,
                        content = "",
                        isModified = false,
                        totalLines = 1,
                        isCloudSynced = true
                    )
                }
            }
        }
    }

    fun handleNewFile() {
        if (_uiState.value.isModified) {
            _uiState.update { state ->
                state.copy(
                    isUnsavedChangesDialogOpen = true,
                    pendingActionAfterUnsaved = { createNewFile() }
                )
            }
        } else {
            createNewFile()
        }
    }

    private fun createNewFile() {
        _uiState.update { state ->
            state.copy(
                fileName = "Untitled.txt",
                filePath = "${state.currentStorageDirectory}/Untitled.txt",
                content = "",
                isModified = false,
                totalLines = 1,
                isCloudSynced = true,
                liveSyncStatusMessage = "New document created"
            )
        }
    }

    fun handleOpenFileRequest() {
        if (_uiState.value.isModified) {
            _uiState.update { state ->
                state.copy(
                    isUnsavedChangesDialogOpen = true,
                    pendingActionAfterUnsaved = {
                        _uiState.update { it.copy(isStorageExplorerOpen = true) }
                    }
                )
            }
        } else {
            _uiState.update { it.copy(isStorageExplorerOpen = true) }
        }
    }

    fun openRecentFile(path: String) {
        if (_uiState.value.isModified) {
            _uiState.update { state ->
                state.copy(
                    isUnsavedChangesDialogOpen = true,
                    pendingActionAfterUnsaved = { loadDocument(path) }
                )
            }
        } else {
            loadDocument(path)
        }
    }

    fun saveAs(newFilePath: String) {
        val name = newFilePath.substringAfterLast("/")
        _uiState.update { state ->
            state.copy(
                fileName = name,
                filePath = newFilePath,
                isSaveAsDialogOpen = false
            )
        }
        saveCurrentDocument()
    }

    fun clearRecentFiles() {
        viewModelScope.launch {
            repository.clearRecents()
        }
    }

    // Collaboration room methods
    fun setCollabRoom(roomId: String) {
        _uiState.update { state ->
            state.copy(
                collabRoomId = roomId,
                isCollabActive = roomId.isNotBlank(),
                liveSyncStatusMessage = if (roomId.isNotBlank()) "Connected to Room $roomId" else "Local mode"
            )
        }
    }

    private fun startSimulatedRealtimeCollaboration() {
        simulatedCollabJob?.cancel()
        simulatedCollabJob = viewModelScope.launch {
            while (true) {
                delay(12000)
                if (_uiState.value.isCollabActive && _uiState.value.collaborators.isNotEmpty()) {
                    val activeCollab = _uiState.value.collaborators.random()
                    _uiState.update { state ->
                        val updatedList = state.collaborators.map { col ->
                            if (col.id == activeCollab.id) {
                                col.copy(
                                    isTyping = true,
                                    activeLine = Random.nextInt(1, maxOf(2, state.totalLines + 1)),
                                    activeColumn = Random.nextInt(1, 10)
                                )
                            } else col.copy(isTyping = false)
                        }
                        state.copy(collaborators = updatedList)
                    }
                    delay(3000)
                    _uiState.update { state ->
                        state.copy(
                            collaborators = state.collaborators.map { it.copy(isTyping = false) }
                        )
                    }
                }
            }
        }
    }

    // Toggle and Selection Handlers
    fun setMenuExpanded(expanded: Boolean) { _uiState.update { it.copy(isMenuExpanded = expanded) } }
    fun setRecentSubmenuExpanded(expanded: Boolean) { _uiState.update { it.copy(isRecentSubmenuExpanded = expanded) } }
    fun setEncodingSubmenuExpanded(expanded: Boolean) { _uiState.update { it.copy(isEncodingSubmenuExpanded = expanded) } }
    fun setOptionsSubmenuExpanded(expanded: Boolean) { _uiState.update { it.copy(isOptionsSubmenuExpanded = expanded) } }
    fun setThemeSubmenuExpanded(expanded: Boolean) { _uiState.update { it.copy(isThemeSubmenuExpanded = expanded) } }
    fun setTextSizeSubmenuExpanded(expanded: Boolean) { _uiState.update { it.copy(isTextSizeSubmenuExpanded = expanded) } }
    fun setTypefaceSubmenuExpanded(expanded: Boolean) { _uiState.update { it.copy(isTypefaceSubmenuExpanded = expanded) } }

    fun setEncoding(encoding: String) {
        _uiState.update { it.copy(encoding = encoding, isEncodingSubmenuExpanded = false, isMenuExpanded = false) }
    }

    fun setTheme(theme: EditorTheme) {
        _uiState.update { it.copy(theme = theme, isThemeSubmenuExpanded = false, isMenuExpanded = false) }
    }

    fun setTextSize(size: EditorTextSize) {
        _uiState.update { it.copy(textSize = size, isTextSizeSubmenuExpanded = false, isMenuExpanded = false) }
    }

    fun setTypeface(typeface: String) {
        _uiState.update { it.copy(typeface = typeface, isTypefaceSubmenuExpanded = false, isMenuExpanded = false) }
    }

    fun toggleOptionViewFiles() {
        _uiState.update { it.copy(options = it.options.copy(viewFiles = !it.options.viewFiles)) }
    }
    fun toggleOptionOpenLast() {
        _uiState.update { it.copy(options = it.options.copy(openLast = !it.options.openLast)) }
    }
    fun toggleOptionAutoSave() {
        _uiState.update { it.copy(options = it.options.copy(autoSave = !it.options.autoSave)) }
    }
    fun toggleOptionWordWrap() {
        _uiState.update { it.copy(options = it.options.copy(wordWrap = !it.options.wordWrap)) }
    }
    fun toggleOptionSuggestions() {
        _uiState.update { it.copy(options = it.options.copy(suggestions = !it.options.suggestions)) }
    }
    fun toggleOptionHighlightSyntax() {
        _uiState.update { it.copy(options = it.options.copy(highlightSyntax = !it.options.highlightSyntax)) }
    }

    fun setSearchMode(enabled: Boolean) {
        _uiState.update { it.copy(isSearchMode = enabled, searchQuery = "", replaceQuery = "", searchMatches = emptyList()) }
    }

    fun setSearchQuery(query: String) {
        val matches = mutableListOf<Int>()
        if (query.isNotEmpty()) {
            var index = _uiState.value.content.indexOf(query, ignoreCase = true)
            while (index >= 0) {
                matches.add(index)
                index = _uiState.value.content.indexOf(query, index + query.length, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(searchQuery = query, searchMatches = matches, currentMatchIndex = 0) }
    }

    fun setReplaceQuery(query: String) {
        _uiState.update { it.copy(replaceQuery = query) }
    }

    fun replaceCurrentMatch() {
        val state = _uiState.value
        if (state.searchMatches.isNotEmpty() && state.currentMatchIndex in state.searchMatches.indices) {
            val matchPos = state.searchMatches[state.currentMatchIndex]
            val newText = state.content.replaceRange(matchPos, matchPos + state.searchQuery.length, state.replaceQuery)
            updateContent(newText)
            setSearchQuery(state.searchQuery)
        }
    }

    fun replaceAllMatches() {
        val state = _uiState.value
        if (state.searchQuery.isNotEmpty()) {
            val newText = state.content.replace(state.searchQuery, state.replaceQuery, ignoreCase = true)
            updateContent(newText)
            setSearchQuery(state.searchQuery)
        }
    }

    fun setSaveAsDialogOpen(open: Boolean) { _uiState.update { it.copy(isSaveAsDialogOpen = open) } }
    fun setStorageExplorerOpen(open: Boolean) { _uiState.update { it.copy(isStorageExplorerOpen = open) } }
    fun setUnsavedChangesDialogOpen(open: Boolean) { _uiState.update { it.copy(isUnsavedChangesDialogOpen = open) } }
    fun setGoToLineDialogOpen(open: Boolean) { _uiState.update { it.copy(isGoToLineDialogOpen = open) } }
    fun setAboutDialogOpen(open: Boolean) { _uiState.update { it.copy(isAboutDialogOpen = open) } }
    fun setCollabDialogOpen(open: Boolean) { _uiState.update { it.copy(isCollabDialogOpen = open) } }
    fun setMarkdownPreviewOpen(open: Boolean) { _uiState.update { it.copy(isMarkdownPreviewOpen = open) } }

    fun goToLine(lineNumber: Int) {
        val targetLine = lineNumber.coerceIn(1, _uiState.value.totalLines)
        _uiState.update { it.copy(cursorLine = targetLine, isGoToLineDialogOpen = false) }
    }

    fun discardUnsavedChanges() {
        val pendingAction = _uiState.value.pendingActionAfterUnsaved
        _uiState.update { it.copy(isUnsavedChangesDialogOpen = false, pendingActionAfterUnsaved = null) }
        pendingAction?.invoke()
    }

    fun saveAndProceedUnsavedChanges() {
        saveCurrentDocument()
        val pendingAction = _uiState.value.pendingActionAfterUnsaved
        _uiState.update { it.copy(isUnsavedChangesDialogOpen = false, pendingActionAfterUnsaved = null) }
        pendingAction?.invoke()
    }
}
