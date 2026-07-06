package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TraceProgress::class, StickerReward::class], version = 2, exportSchema = false)
abstract class TraceDatabase : RoomDatabase() {
    abstract fun traceDao(): TraceDao

    companion object {
        @Volatile
        private var INSTANCE: TraceDatabase? = null

        fun getDatabase(context: Context): TraceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TraceDatabase::class.java,
                    "trace_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
