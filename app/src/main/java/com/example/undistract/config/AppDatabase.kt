package com.example.undistract.config

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.block_permanent.data.local.BlockPermanentDao
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitDao
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import com.example.undistract.features.variable_session.data.local.VariableSessionDao
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity

@Database(entities = [BlockSchedulesEntity::class, VariableSessionEntity::class, BlockPermanentEntity::class, SetaDailyLimitEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockSchedulesDao(): BlockSchedulesDao
    abstract fun variableSessionDao(): VariableSessionDao
    abstract fun blockPermanentDao(): BlockPermanentDao
    abstract fun setaDailyLimitDao(): SetaDailyLimitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    Log.d(TAG, "Creating new database instance")
                    val MIGRATION_1_2 = object : Migration(1, 2) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE daily_limits_table ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
                        }
                    }

                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    Log.d(TAG, "Database instance created successfully")
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating database", e)
                    throw e
                }
            }
        }
    }
}