package com.example.undistract.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import com.example.undistract.features.variable_session.data.local.VariableSessionDao
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity

@Database(entities = [BlockSchedulesEntity::class, VariableSessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockSchedulesDao(): BlockSchedulesDao
    abstract fun variableSessionDao(): VariableSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}