package com.example.workassistance.ui.site

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workassistance.data.remote.model.Site
import com.example.workassistance.repository.AttendanceRepository
import com.example.workassistance.util.Resource
import kotlinx.coroutines.launch

class SiteViewModel(private val attendanceRepository: AttendanceRepository) : ViewModel() {

    private val _signInState = MutableLiveData<Boolean>()
    /** True when the worker is currently signed in to the loaded site. */
    val isSignedIn: LiveData<Boolean> = _signInState

    private val _eventResult = MutableLiveData<Resource<Unit>>()
    val eventResult: LiveData<Resource<Unit>> = _eventResult

    fun loadSignInState(siteId: String) {
        viewModelScope.launch {
            val last = attendanceRepository.getLastEventType(siteId)
            _signInState.value = last == "SIGN_IN"
        }
    }

    fun signIn(site: Site, latitude: Double, longitude: Double) {
        recordEvent(site, "SIGN_IN", latitude, longitude)
    }

    fun signOut(site: Site, latitude: Double, longitude: Double) {
        recordEvent(site, "SIGN_OUT", latitude, longitude)
    }

    private fun recordEvent(site: Site, eventType: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _eventResult.value = Resource.Loading
            val result = attendanceRepository.recordEvent(
                siteId = site.id,
                siteName = site.name,
                eventType = eventType,
                latitude = latitude,
                longitude = longitude
            )
            _eventResult.value = result
            // Refresh signed-in state
            _signInState.value = eventType == "SIGN_IN"
        }
    }
}
