package com.example.workassistance.repository

import com.example.workassistance.data.remote.api.ApiService
import com.example.workassistance.data.remote.model.Site
import com.example.workassistance.util.Resource

class SiteRepository(private val apiService: ApiService) {

    suspend fun getSites(): Resource<List<Site>> {
        return try {
            val response = apiService.getSites()
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
