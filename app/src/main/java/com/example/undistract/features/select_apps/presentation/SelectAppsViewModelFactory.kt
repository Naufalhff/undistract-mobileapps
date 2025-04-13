package com.example.undistract.features.select_apps.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.get_installed_apps.data.InstalledAppsRepository
import com.example.undistract.features.get_installed_apps.domain.GetInstalledAppsUseCase
import com.example.undistract.features.select_apps.data.SelectAppsRepository

class SelectAppsViewModelFactory(
    private val context: Context,
    private val selectAppsRepository: SelectAppsRepository = SelectAppsRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectAppsViewModel::class.java)) {
            val repository = InstalledAppsRepository(context)
            val useCase = GetInstalledAppsUseCase(repository)
            @Suppress("UNCHECKED_CAST")
            return SelectAppsViewModel(useCase, selectAppsRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}