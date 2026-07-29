package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String, // filePath or UUID
    val fileName: String,
    val filePath: String,
    val content: String,
    val encoding: String = "UTF-8",
    val lastModified: Long = System.currentTimeMillis(),
    val isCloudSynced: Boolean = true,
    val cloudRoomId: String? = null
)
