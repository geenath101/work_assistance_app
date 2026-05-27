package com.example.workassistance.data.remote.api

import com.example.workassistance.data.remote.model.AttendanceRequest
import com.example.workassistance.data.remote.model.AttendanceResponse
import com.example.workassistance.data.remote.model.LoginRequest
import com.example.workassistance.data.remote.model.LoginResponse
import com.example.workassistance.data.remote.model.SiteAssignment
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    /**
     * Fetch site assignments for a specific employee.
     * GET /sites/employee/{employeeId}
     *
     * Each item in the response contains the site's coordinates and allowed
     * sign-in radius, so no secondary API call is needed for geofence checks.
     */
    @GET("employees/employee/{employeeId}")
    suspend fun getSitesByEmployee(@Path("employeeId") employeeId: String): Response<List<SiteAssignment>>

    /**
     * Record a sign-in or sign-out event.
     */
    @POST("attendance")
    suspend fun postAttendance(@Body request: AttendanceRequest): Response<AttendanceResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
