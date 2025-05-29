package com.example.undistract.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.add_limit.presentation.AddLimitViewModelFactory
import com.example.undistract.features.block_permanent.presentation.BlockPermanentViewModelFactory
import com.example.undistract.features.block_schedules.data.BlockSchedulesViewModelFactory
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModelFactory
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModelFactory

object ViewModelFactoryProvider {

    fun provideSelectAppsViewModelFactory(context: Context): ViewModelProvider.Factory {
        val visitedUrlsRepo = AppDependenciesProvider.provideVisitedUrlsRepository(context)
        val selectAppsRepo = AppDependenciesProvider.provideSelectAppsRepository()
        return SelectAppsViewModelFactory(context, selectAppsRepo, visitedUrlsRepo)
    }

    fun provideAddLimitViewModelFactory(): ViewModelProvider.Factory {
        return AddLimitViewModelFactory()
    }

    fun provideBlockPermanentViewModelFactory(context: Context, isParental: Boolean): ViewModelProvider.Factory {
        val blockPermanentRepo = AppDependenciesProvider.provideBlockPermanentRepository(context)
        return BlockPermanentViewModelFactory(blockPermanentRepo, isParental)
    }

    fun provideBlockSchedulesViewModelFactory(context: Context, isParental: Boolean): ViewModelProvider.Factory {
        val repository = AppDependenciesProvider.provideBlockSchedulesRepository(context)
        return BlockSchedulesViewModelFactory(repository, isParental)
    }

    fun provideVariableSessionViewModelFactory(context: Context, isParental: Boolean): ViewModelProvider.Factory {
        val variableSessionRepo = AppDependenciesProvider.provideVariableSessionRepository(context)
        return VariableSessionViewModelFactory(variableSessionRepo, isParental)
    }
}
