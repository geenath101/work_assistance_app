package com.example.workassistance.data.remote.model

import com.google.gson.annotations.SerializedName

data class AttendanceRequest(
    @SerializedName("site_id") val siteId: String,
    @SerializedName("site_name") val siteName: String,
    @SerializedName("event_type") val eventType: String,   // "SIGN_IN" or "SIGN_OUT"
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: Long       // epoch millis
)

data class AttendanceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)
