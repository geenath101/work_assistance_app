package com.example.workassistance.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workassistance.data.remote.model.Site
import com.example.workassistance.repository.SiteRepository
import com.example.workassistance.util.Resource
import kotlinx.coroutines.launch

class MainViewModel(private val siteRepository: SiteRepository) : ViewModel() {

    private val _sites = MutableLiveData<Resource<List<Site>>>()
    val sites: LiveData<Resource<List<Site>>> = _sites

    init {
        loadSites()
    }

    fun loadSites() {
        viewModelScope.launch {
            _sites.value = Resource.Loading
            _sites.value = siteRepository.getSites()
        }
    }
}
