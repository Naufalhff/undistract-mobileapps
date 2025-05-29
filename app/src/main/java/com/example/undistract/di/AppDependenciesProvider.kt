package com.example.undistract.di

import android.content.Context
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.get_visited_urls.data.VisitedUrlsRepository
import com.example.undistract.features.select_apps.data.SelectAppsRepository
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.variable_session.data.VariableSessionRepository

object AppDependenciesProvider {

    private fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    fun provideSelectAppsRepository(): SelectAppsRepository {
        return SelectAppsRepository()
    }

    fun provideBlockPermanentRepository(context: Context): BlockPermanentRepository {
        val dao = provideDatabase(context).blockPermanentDao()
        return BlockPermanentRepository(dao)
    }

    fun provideBlockSchedulesRepository(context: Context): BlockSchedulesRepository {
        val database = AppDatabase.getDatabase(context)
        val dao = database.blockSchedulesDao()
        return BlockSchedulesRepository(dao)
    }

    fun provideVariableSessionRepository(context: Context): VariableSessionRepository {
        val dao = provideDatabase(context).variableSessionDao()
        return VariableSessionRepository(dao)
    }

    fun provideVisitedUrlsRepository(context: Context): VisitedUrlsRepository {
        val dao = provideDatabase(context).visitedUrlsDao()
        return VisitedUrlsRepository(dao)
    }

    fun provideSetaDailyLimitRepository(context: Context): SetaDailyLimitRepository {
        val database = AppDatabase.getDatabase(context)
        val dao = database.setaDailyLimitDao()
        return SetaDailyLimitRepositoryImpl(dao)
    }
}
