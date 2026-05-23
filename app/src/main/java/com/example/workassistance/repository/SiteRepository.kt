package com.example.workassistance.repository

import com.example.workassistance.data.remote.api.ApiService
import com.example.workassistance.data.remote.model.SiteAssignment
import com.example.workassistance.util.Resource

class SiteRepository(private val apiService: ApiService) {

    /**
     * Fetch all site assignments for the given employee.
     * Calls GET /sites/employee/{employeeId}.
     *
     * The response already contains latitude, longitude, and radius for each
     * site, so geofence checks can be performed locally without a second call.
     */
    suspend fun getSitesByEmployee(employeeId: String): Resource<List<SiteAssignment>> {
        return try {
            val response = apiService.getSitesByEmployee(employeeId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load sites: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
