package com.example.workassistance.ui.site

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.workassistance.data.local.entity.SiteRequestEntity
import com.example.workassistance.repository.SiteRequestRepository
import com.example.workassistance.util.Resource
import kotlinx.coroutines.launch

class SiteRequestsViewModel(
    private val repo: SiteRequestRepository
) : ViewModel() {

    private val siteId = MutableLiveData<String>()

    val requests: LiveData<List<SiteRequestEntity>> = siteId.switchMap { id ->
        repo.observeRequests(id)
    }

    private val _saveResult = MutableLiveData<Resource<Unit>?>()
    val saveResult: LiveData<Resource<Unit>?> = _saveResult

    fun setSite(siteId: String) {
        this.siteId.value = siteId
    }

    fun createRequest(title: String, description: String, quantity: Int?) {
        val id = siteId.value ?: return
        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            _saveResult.value = repo.createRequest(id, title, description, quantity)
        }
    }

    fun showError(message: String) {
        _saveResult.value = Resource.Error(message)
    }
}

class SiteRequestsViewModelFactory(
    private val repo: SiteRequestRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SiteRequestsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SiteRequestsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
