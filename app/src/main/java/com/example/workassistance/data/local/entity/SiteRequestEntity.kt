package com.example.workassistance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "site_requests")
data class SiteRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: String,
    val title: String,
    val description: String,
    val quantity: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** Placeholder for future lifecycle: SUBMITTED/APPROVED/REJECTED/FULFILLED, etc. */
    val status: String = "DRAFT",
    /** Placeholder for future remote sync. */
    val synced: Boolean = false
)
