package com.example.workassistance.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.repository.SiteRepository
import com.example.workassistance.util.Resource
import kotlinx.coroutines.launch

class MainViewModel(
    private val siteRepository: SiteRepository,
    private val employeeId: String
) : ViewModel() {

    private val _sites = MutableLiveData<Resource<List<SiteAssignment>>>()
    val sites: LiveData<Resource<List<SiteAssignment>>> = _sites

    init {
        loadSites()
    }

    fun loadSites() {
        viewModelScope.launch {
            _sites.value = Resource.Loading
            _sites.value = siteRepository.getSitesByEmployee(employeeId)
        }
    }
}
