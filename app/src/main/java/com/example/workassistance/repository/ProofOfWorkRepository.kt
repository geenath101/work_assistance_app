package com.example.workassistance.repository

import androidx.lifecycle.LiveData
import com.example.workassistance.data.local.dao.ProofOfWorkDao
import com.example.workassistance.data.local.entity.ProofOfWorkPhoto
import com.example.workassistance.util.Resource

class ProofOfWorkRepository(
    private val dao: ProofOfWorkDao
) {
    fun observePhotos(siteId: String): LiveData<List<ProofOfWorkPhoto>> = dao.observeBySite(siteId)

    suspend fun addPhoto(siteId: String, uri: String, note: String?): Resource<Unit> {
        return try {
            dao.insert(
                ProofOfWorkPhoto(
                    siteId = siteId,
                    uri = uri,
                    note = note
                )
            )
            // Placeholder: later call remote API, then mark synced.
            Resource.Success(Unit)
        } catch (t: Throwable) {
            Resource.Error("Failed to save photo locally: ${t.message ?: "unknown error"}")
        }
    }

    suspend fun deletePhoto(id: Long): Resource<Unit> {
        return try {
            dao.deleteById(id)
            Resource.Success(Unit)
        } catch (t: Throwable) {
            Resource.Error("Failed to delete photo: ${t.message ?: "unknown error"}")
        }
    }
}
