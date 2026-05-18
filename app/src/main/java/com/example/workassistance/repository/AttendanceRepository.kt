package com.example.workassistance.repository

import com.example.workassistance.data.local.dao.AttendanceDao
import com.example.workassistance.data.local.entity.AttendanceRecord
import com.example.workassistance.data.remote.api.ApiService
import com.example.workassistance.data.remote.model.AttendanceRequest
import com.example.workassistance.util.Resource

class AttendanceRepository(
    private val attendanceDao: AttendanceDao,
    private val apiService: ApiService
) {

    val allRecords = attendanceDao.getAllRecords()

    /**
     * Returns the last recorded event type for the given site ("SIGN_IN" / "SIGN_OUT" / null).
     * Used to determine whether the worker is currently signed in.
     */
    suspend fun getLastEventType(siteId: String): String? =
        attendanceDao.getLastEventForSite(siteId)?.eventType

    /**
     * Records the event locally first, then attempts to sync to the server.
     * If the server call fails the record stays with synced=false for later retry.
     */
    suspend fun recordEvent(
        siteId: String,
        siteName: String,
        eventType: String,
        latitude: Double,
        longitude: Double
    ): Resource<Unit> {
        val timestamp = System.currentTimeMillis()

        // 1. Persist locally immediately
        val record = AttendanceRecord(
            siteId = siteId,
            siteName = siteName,
            eventType = eventType,
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp,
            synced = false
        )
        val localId = attendanceDao.insert(record)

        // 2. Attempt remote sync
        return try {
            val request = AttendanceRequest(
                siteId = siteId,
                siteName = siteName,
                eventType = eventType,
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp
            )
            val response = apiService.postAttendance(request)
            if (response.isSuccessful) {
                attendanceDao.markSynced(localId)
                Resource.Success(Unit)
            } else {
                // Saved locally; will retry later
                Resource.Error("Saved locally. Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            // Saved locally; no network
            Resource.Error("Saved locally. No network: ${e.localizedMessage}")
        }
    }

    /** Retry syncing any records that failed to reach the server. */
    suspend fun syncPending() {
        val unsynced = attendanceDao.getUnsynced()
        for (record in unsynced) {
            try {
                val request = AttendanceRequest(
                    siteId = record.siteId,
                    siteName = record.siteName,
                    eventType = record.eventType,
                    latitude = record.latitude,
                    longitude = record.longitude,
                    timestamp = record.timestamp
                )
                val response = apiService.postAttendance(request)
                if (response.isSuccessful) {
                    attendanceDao.markSynced(record.id)
                }
            } catch (_: Exception) {
                // Will retry on next call
            }
        }
    }
}
