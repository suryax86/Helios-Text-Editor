package com.example.data.model

enum class EditorTheme(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System"),
    WHITE("White"),
    BLACK("Black"),
    RETRO("Retro")
}

enum class EditorTextSize(val label: String, val spSize: Int) {
    SMALL("Small", 13),
    MEDIUM("Medium", 16),
    LARGE("Large", 20)
}

data class EditorOptions(
    val viewFiles: Boolean = true,
    val openLast: Boolean = false,
    val autoSave: Boolean = false,
    val wordWrap: Boolean = false,
    val suggestions: Boolean = true,
    val highlightSyntax: Boolean = true
)

data class Collaborator(
    val id: String,
    val name: String,
    val colorHex: Long,
    val activeLine: Int = 1,
    val activeColumn: Int = 1,
    val isTyping: Boolean = false,
    val lastTypedSnippet: String = ""
)

data class StorageNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val itemCount: Int = 0,
    val sizeText: String = "0 KB"
)
