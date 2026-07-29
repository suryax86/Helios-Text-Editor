package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DocumentEntity
import com.example.data.local.RecentFileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DocumentRepository(private val db: AppDatabase) {
    val recentFiles: Flow<List<RecentFileEntity>> = db.recentFileDao().getRecentFiles()

    suspend fun initializeDefaultDataIfNeeded() {
        val currentRecents = db.recentFileDao().getRecentFiles().first()
        if (currentRecents.isEmpty()) {
            val defaults = listOf(
                RecentFileEntity(path = "/sdcard/customisation.txt", fileName = "customisation.txt"),
                RecentFileEntity(path = "api/release.b64", fileName = "release.b64"),
                RecentFileEntity(path = "api/git-token.key", fileName = "git-token.key"),
                RecentFileEntity(path = "api/VirusTotal-api.key", fileName = "VirusTotal-api.key")
            )
            defaults.forEach { db.recentFileDao().insertRecent(it) }

            // Pre-seed default Editor.txt document
            val defaultDoc = DocumentEntity(
                id = "/storage/emulated/99/Documents/Editor.txt",
                fileName = "Editor.txt",
                filePath = "/storage/emulated/99/Documents/Editor.txt",
                content = "ygggg\n\n// Real-time collaborative document\n// Edit text, change encodings, themes, or start a live session!",
                encoding = "UTF-8"
            )
            db.documentDao().insertDocument(defaultDoc)
        }
    }

    suspend fun saveDocument(doc: DocumentEntity) {
        db.documentDao().insertDocument(doc)
        db.recentFileDao().insertRecent(
            RecentFileEntity(
                path = doc.filePath,
                fileName = doc.fileName,
                openedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getDocument(path: String): DocumentEntity? {
        return db.documentDao().getDocumentByPath(path)
    }

    suspend fun clearRecents() {
        db.recentFileDao().clearAll()
    }
}
