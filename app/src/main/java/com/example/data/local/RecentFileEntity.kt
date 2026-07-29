package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String,
    val fileName: String,
    val openedAt: Long = System.currentTimeMillis()
)
