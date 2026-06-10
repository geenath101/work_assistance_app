package com.example.workassistance.ui.site

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.workassistance.data.local.entity.ProofOfWorkPhoto
import com.example.workassistance.repository.ProofOfWorkRepository
import com.example.workassistance.util.Resource
import kotlinx.coroutines.launch

class ProofOfWorkViewModel(
    private val repo: ProofOfWorkRepository
) : ViewModel() {

    private val scopeKey = MutableLiveData<ScopeKey>()

    private data class ScopeKey(
        val siteId: String,
        val taskId: String?
    )

    val photos: LiveData<List<ProofOfWorkPhoto>> = scopeKey.switchMap { key ->
        if (key.taskId == null) repo.observePhotos(key.siteId)
        else repo.observePhotos(key.siteId, key.taskId)
    }

    private val _saveResult = MutableLiveData<Resource<Unit>?>()
    val saveResult: LiveData<Resource<Unit>?> = _saveResult

    fun setScope(siteId: String, taskId: String?) {
        scopeKey.value = ScopeKey(siteId = siteId, taskId = taskId)
    }

    fun addPhoto(uri: String, note: String?) {
        val key = scopeKey.value ?: return
        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            _saveResult.value = repo.addPhoto(key.siteId, key.taskId, uri, note)
        }
    }

    fun delete(photoId: Long) {
        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            _saveResult.value = repo.deletePhoto(photoId)
        }
    }
}

class ProofOfWorkViewModelFactory(private val repo: ProofOfWorkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProofOfWorkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProofOfWorkViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
