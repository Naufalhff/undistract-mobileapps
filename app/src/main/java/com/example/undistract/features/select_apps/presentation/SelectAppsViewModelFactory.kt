package com.example.undistract.features.select_apps.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.get_app_data.data.AppDataRepository
import com.example.undistract.features.get_visited_urls.data.VisitedUrlsRepository
import com.example.undistract.features.select_apps.data.SelectAppsRepository

class SelectAppsViewModelFactory(
    private val context: Context,
    private val selectAppsRepository: SelectAppsRepository = SelectAppsRepository(),
    private val visitedUrlsRepository: VisitedUrlsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectAppsViewModel::class.java)) {
            val appDataRepository = AppDataRepository(context, visitedUrlsRepository)
            @Suppress("UNCHECKED_CAST")
            return SelectAppsViewModel(appDataRepository, selectAppsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

