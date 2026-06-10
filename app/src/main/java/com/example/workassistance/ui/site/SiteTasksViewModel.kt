package com.example.workassistance.ui.site

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.workassistance.data.local.entity.SiteTaskEntity
import com.example.workassistance.repository.AuthRepository
import com.example.workassistance.repository.SiteTaskRepository
import com.example.workassistance.util.Resource
import kotlinx.coroutines.launch

class SiteTasksViewModel(
    private val repo: SiteTaskRepository
) : ViewModel() {

    private val siteId = MutableLiveData<String>()
    val tasks: LiveData<List<SiteTaskEntity>> = siteId.switchMap { id -> repo.observeTasks(id) }

    private val _loadResult = MutableLiveData<Resource<Unit>?>()
    val loadResult: LiveData<Resource<Unit>?> = _loadResult

    private val _actionResult = MutableLiveData<Resource<Unit>?>()
    val actionResult: LiveData<Resource<Unit>?> = _actionResult

    fun setSite(siteId: String) {
        if (this.siteId.value == siteId) return
        this.siteId.value = siteId
    }

    fun refresh() {
        val id = siteId.value ?: return
        _loadResult.value = Resource.Loading
        viewModelScope.launch {
            _loadResult.value = repo.refresh(id)
        }
    }

    fun completeTask(taskId: String) {
        val sId = siteId.value ?: return
        val employeeId = AuthRepository.getCurrentUser()?.employeeId ?: ""
        _actionResult.value = Resource.Loading
        viewModelScope.launch {
            _actionResult.value = repo.completeTask(siteId = sId, taskId = taskId, employeeId = employeeId)
        }
    }

    fun showError(message: String) {
        _actionResult.value = Resource.Error(message)
    }
}

class SiteTasksViewModelFactory(
    private val repo: SiteTaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SiteTasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SiteTasksViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
