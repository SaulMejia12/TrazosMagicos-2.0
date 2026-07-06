package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TraceDao {
    @Query("SELECT * FROM trace_progress WHERE profileId = :profileId")
    fun getProgressForProfile(profileId: String): Flow<List<TraceProgress>>

    @Query("SELECT * FROM trace_progress WHERE profileId = :profileId AND charId = :charId LIMIT 1")
    suspend fun getProgressForChar(profileId: String, charId: String): TraceProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: TraceProgress)

    @Query("SELECT * FROM sticker_rewards WHERE profileId = :profileId ORDER BY unlockTime DESC")
    fun getStickersForProfile(profileId: String): Flow<List<StickerReward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSticker(sticker: StickerReward)

    @Query("DELETE FROM trace_progress WHERE profileId = :profileId")
    suspend fun clearProgressForProfile(profileId: String)

    @Query("DELETE FROM sticker_rewards WHERE profileId = :profileId")
    suspend fun clearStickersForProfile(profileId: String)

    @Query("DELETE FROM trace_progress")
    suspend fun clearAllProgress()

    @Query("DELETE FROM sticker_rewards")
    suspend fun clearAllStickers()
}
