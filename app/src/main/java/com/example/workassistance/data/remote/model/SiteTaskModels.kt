package com.example.workassistance.data.remote.model

import com.google.gson.annotations.SerializedName

data class SiteTaskDto(
    @SerializedName("id") val id: String,
    @SerializedName("site_id") val siteId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("due_at") val dueAt: Long? = null,
    @SerializedName("priority") val priority: String = "NORMAL",
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("completed_at") val completedAt: Long? = null
)

data class CompleteTaskRequest(
    @SerializedName("task_id") val taskId: String,
    @SerializedName("employee_id") val employeeId: String,
    @SerializedName("proof_photo_uris") val proofPhotoUris: List<String>
)

data class CompleteTaskResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
