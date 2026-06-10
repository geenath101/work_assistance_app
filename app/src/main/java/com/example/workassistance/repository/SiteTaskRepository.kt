package com.example.workassistance.repository

import androidx.lifecycle.LiveData
import com.example.workassistance.data.local.dao.ProofOfWorkDao
import com.example.workassistance.data.local.dao.SiteTaskDao
import com.example.workassistance.data.local.entity.SiteTaskEntity
import com.example.workassistance.data.remote.api.ApiService
import com.example.workassistance.data.remote.model.CompleteTaskRequest
import com.example.workassistance.util.Resource

class SiteTaskRepository(
    private val siteTaskDao: SiteTaskDao,
    private val proofDao: ProofOfWorkDao,
    private val api: ApiService
) {

    fun observeTasks(siteId: String): LiveData<List<SiteTaskEntity>> = siteTaskDao.observeBySite(siteId)

    suspend fun refresh(siteId: String): Resource<Unit> {
        return try {
            val resp = api.getTasksBySite(siteId)
            if (!resp.isSuccessful) {
                return Resource.Error("Server error: ${resp.code()}")
            }
            val dtos = resp.body().orEmpty()
            siteTaskDao.upsertAll(
                dtos.map {
                    SiteTaskEntity(
                        taskId = it.id,
                        siteId = it.siteId,
                        title = it.title,
                        description = it.description,
                        createdAt = it.createdAt,
                        dueAt = it.dueAt,
                        priority = it.priority,
                        isCompleted = it.isCompleted,
                        completedAt = it.completedAt,
                        synced = true
                    )
                }
            )
            Resource.Success(Unit)
        } catch (t: Throwable) {
            Resource.Error("Failed to load tasks: ${t.message ?: "unknown error"}")
        }
    }

    suspend fun completeTask(siteId: String, taskId: String, employeeId: String): Resource<Unit> {
        return try {
            // The backend requires at least 1 proof photo; validate locally.
            val count = proofDao.countBySiteTask(siteId, taskId)
            if (count <= 0) return Resource.Error("Attach at least 1 proof photo before completing")

            // For now we send local Uri strings as proof references.
            val proofUris = proofDao.getUrisBySiteTask(siteId, taskId)
            val resp = api.completeTask(
                CompleteTaskRequest(
                    taskId = taskId,
                    employeeId = employeeId,
                    proofPhotoUris = proofUris
                )
            )
            if (!resp.isSuccessful) {
                return Resource.Error("Server error: ${resp.code()}")
            }
            val body = resp.body()
            if (body == null || !body.success) {
                return Resource.Error(body?.message ?: "Failed to complete")
            }

            siteTaskDao.setCompleted(taskId = taskId, completed = true, completedAt = System.currentTimeMillis())
            Resource.Success(Unit)
        } catch (t: Throwable) {
            Resource.Error("Failed to complete: ${t.message ?: "unknown error"}")
        }
    }
}
