package com.example.workassistance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "site_tasks")
data class SiteTaskEntity(
    /** Remote id (mocked for now). */
    @PrimaryKey val taskId: String,
    val siteId: String,
    val title: String,
    val description: String,
    val createdAt: Long,
    val dueAt: Long? = null,
    val priority: String = "NORMAL",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    /** Placeholder for future remote sync. */
    val synced: Boolean = false
)
