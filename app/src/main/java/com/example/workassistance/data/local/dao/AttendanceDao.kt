package com.example.workassistance.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.workassistance.data.local.entity.AttendanceRecord

@Dao
interface AttendanceDao {

    @Insert
    suspend fun insert(record: AttendanceRecord): Long

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): LiveData<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE synced = 0")
    suspend fun getUnsynced(): List<AttendanceRecord>

    @Query("UPDATE attendance_records SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    /** Returns the last event for a given site so we know if worker is currently signed in. */
    @Query("SELECT * FROM attendance_records WHERE siteId = :siteId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEventForSite(siteId: String): AttendanceRecord?
}
