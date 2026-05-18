package com.example.workassistance.ui.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.workassistance.repository.AttendanceRepository

class SiteViewModelFactory(private val repo: AttendanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SiteViewModel(repo) as T
    }
}
