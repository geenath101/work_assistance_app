package com.example.workassistance.data.remote.model

import com.google.gson.annotations.SerializedName

data class ShiftSlot(
    @SerializedName("day")             val day: String,
    @SerializedName("shift_start")     val shiftStart: String,
    @SerializedName("shift_end")       val shiftEnd: String,
    @SerializedName("allocated_hours") val allocatedHours: Int
)

/**
 * Represents one employee-site assignment as returned by
 * GET /sites/employee/{employeeId}.
 *
 * All location and geofence data needed for sign-in enforcement is embedded
 * directly in this response — no secondary API call is required.
 */
data class SiteAssignment(
    @SerializedName("id")           val assignmentId: String,
    @SerializedName("site_id")      val siteId: String,
    @SerializedName("site_name")    val siteName: String,
    @SerializedName("site_address") val siteAddress: String,
    @SerializedName("site_latitude")  val latitude: Double,
    @SerializedName("site_longitude") val longitude: Double,
    @SerializedName("site_radius_m")  val radiusMeters: Double,
    @SerializedName("employee_id")  val employeeId: String,
    @SerializedName("assigned_at")  val assignedAt: String,
    @SerializedName("active")       val active: Boolean,
    @SerializedName("shift_slots")  val shiftSlots: List<ShiftSlot> = emptyList()
)
