package com.example.workassistance.ui.site

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.repository.AttendanceRepository
import com.example.workassistance.util.Resource
import kotlinx.coroutines.launch

class SiteViewModel(private val attendanceRepository: AttendanceRepository) : ViewModel() {

    private val _signInState = MutableLiveData<Boolean>()
    /** True when the worker is currently signed in to the loaded site. */
    val isSignedIn: LiveData<Boolean> = _signInState

    private val _eventResult = MutableLiveData<Resource<Unit>>()
    val eventResult: LiveData<Resource<Unit>> = _eventResult

    fun loadSignInState(siteId: String, signInExpiryMinutes: Int) {
        viewModelScope.launch {
            val last = attendanceRepository.getLastEventType(siteId)
            if (last != "SIGN_IN") {
                _signInState.value = false
                return@launch
            }

            val signInRecord = attendanceRepository.getLastSignInRecord(siteId)
            val signInTs = signInRecord?.timestamp
            val expiryMs = signInExpiryMinutes.coerceAtLeast(1).toLong() * 60_000L
            val expired = signInTs == null || (System.currentTimeMillis() - signInTs) >= expiryMs

            if (expired) {
                // Lazy expiry: record a local SIGN_OUT so state is consistent across restarts.
                // No remote call here (avoids background/network work).
                if (signInRecord != null) {
                    attendanceRepository.insertLocalEvent(
                        siteId = signInRecord.siteId,
                        siteName = signInRecord.siteName,
                        eventType = "SIGN_OUT",
                        latitude = signInRecord.latitude,
                        longitude = signInRecord.longitude
                    )
                }
                _signInState.value = false
            } else {
                _signInState.value = true
            }
        }
    }

    fun signIn(site: SiteAssignment, latitude: Double, longitude: Double) {
        recordEvent(site, "SIGN_IN", latitude, longitude)
    }

    fun signOut(site: SiteAssignment, latitude: Double, longitude: Double) {
        recordEvent(site, "SIGN_OUT", latitude, longitude)
    }

    private fun recordEvent(site: SiteAssignment, eventType: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _eventResult.value = Resource.Loading
            val result = attendanceRepository.recordEvent(
                siteId = site.siteId,
                siteName = site.siteName,
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
