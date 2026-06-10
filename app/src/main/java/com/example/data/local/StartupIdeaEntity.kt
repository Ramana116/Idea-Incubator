package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "startup_ideas",
    indices = [androidx.room.Index(value = ["timestamp"])]
)
data class StartupIdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val industry: String,
    val targetAudience: String,
    val timestamp: Long,
    val reportJson: String?
)
