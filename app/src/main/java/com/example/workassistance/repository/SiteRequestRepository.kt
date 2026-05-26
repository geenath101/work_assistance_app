package com.example.workassistance.repository

import androidx.lifecycle.LiveData
import com.example.workassistance.data.local.dao.SiteRequestDao
import com.example.workassistance.data.local.entity.SiteRequestEntity
import com.example.workassistance.util.Resource

class SiteRequestRepository(
    private val dao: SiteRequestDao
) {
    fun observeRequests(siteId: String): LiveData<List<SiteRequestEntity>> = dao.observeBySite(siteId)

    suspend fun createRequest(
        siteId: String,
        title: String,
        description: String,
        quantity: Int?
    ): Resource<Unit> {
        return try {
            dao.insert(
                SiteRequestEntity(
                    siteId = siteId,
                    title = title,
                    description = description,
                    quantity = quantity,
                    status = "DRAFT"
                )
            )
            // Placeholder: later call remote API, then mark synced + update status.
            Resource.Success(Unit)
        } catch (t: Throwable) {
            Resource.Error("Failed to save request locally: ${t.message ?: "unknown error"}")
        }
    }
}
