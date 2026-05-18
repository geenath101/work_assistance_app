package com.example.workassistance.data.remote.api

import com.example.workassistance.data.remote.model.AttendanceRequest
import com.example.workassistance.data.remote.model.AttendanceResponse
import com.example.workassistance.data.remote.model.Site
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    /**
     * Fetch all configured work sites from the web portal backend.
     * Expected response: JSON array of Site objects.
     * TODO: Replace path with your actual endpoint once confirmed.
     */
    @GET("sites")
    suspend fun getSites(): Response<List<Site>>

    /**
     * Record a sign-in or sign-out event.
     * TODO: Replace path with your actual endpoint once confirmed.
     */
    @POST("attendance")
    suspend fun postAttendance(@Body request: AttendanceRequest): Response<AttendanceResponse>
}
