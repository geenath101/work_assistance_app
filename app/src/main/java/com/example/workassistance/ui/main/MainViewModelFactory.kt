package com.example.workassistance.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.workassistance.repository.SiteRepository

class MainViewModelFactory(
    private val repo: SiteRepository,
    private val employeeId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repo, employeeId) as T
    }
}
