package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trace_progress", primaryKeys = ["profileId", "charId"])
data class TraceProgress(
    val profileId: String,
    val charId: String, // e.g., "A", "1"
    val charType: String,          // "LETTER" or "NUMBER"
    val completedCount: Int = 0,
    val bestAccuracy: Int = 0,
    val starsEarned: Int = 0,
    val lastCompletedTime: Long = 0L
)

@Entity(tableName = "sticker_rewards")
data class StickerReward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: String,
    val stickerId: String,          // e.g., "dino", "rocket", "star"
    val unlockTime: Long
)
