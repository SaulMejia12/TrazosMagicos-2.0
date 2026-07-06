package com.example.data.repository

import com.example.data.local.TraceDao
import com.example.data.local.TraceProgress
import com.example.data.local.StickerReward
import kotlinx.coroutines.flow.Flow

class TraceRepository(private val traceDao: TraceDao) {
    
    fun getProgressForProfile(profileId: String): Flow<List<TraceProgress>> {
        return traceDao.getProgressForProfile(profileId)
    }

    fun getStickersForProfile(profileId: String): Flow<List<StickerReward>> {
        return traceDao.getStickersForProfile(profileId)
    }

    suspend fun getProgressForChar(profileId: String, charId: String): TraceProgress? {
        return traceDao.getProgressForChar(profileId, charId)
    }

    suspend fun saveProgress(progress: TraceProgress) {
        traceDao.insertProgress(progress)
    }

    suspend fun saveSticker(sticker: StickerReward) {
        traceDao.insertSticker(sticker)
    }

    suspend fun resetProfileData(profileId: String) {
        traceDao.clearProgressForProfile(profileId)
        traceDao.clearStickersForProfile(profileId)
    }

    suspend fun resetAllData() {
        traceDao.clearAllProgress()
        traceDao.clearAllStickers()
    }
}
