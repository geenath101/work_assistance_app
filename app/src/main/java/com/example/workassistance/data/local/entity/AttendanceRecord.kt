package com.example.workassistance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: String,
    val siteName: String,
    val eventType: String,       // "SIGN_IN" or "SIGN_OUT"
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,         // epoch millis
    val synced: Boolean = false  // true once successfully POSTed to server
)
