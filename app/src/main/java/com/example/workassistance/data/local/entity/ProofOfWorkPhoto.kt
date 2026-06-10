package com.example.workassistance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proof_of_work_photos")
data class ProofOfWorkPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: String,
    /** Optional link to a specific site task (remote id). */
    val taskId: String? = null,
    /** Stored as a string so we can keep it across process restarts. */
    val uri: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** Placeholder for future remote sync. */
    val synced: Boolean = false
)
