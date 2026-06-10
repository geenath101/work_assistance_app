package com.example.workassistance.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.workassistance.data.local.entity.SiteTaskEntity

@Dao
interface SiteTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<SiteTaskEntity>)

    @Query("SELECT * FROM site_tasks WHERE siteId = :siteId ORDER BY createdAt DESC")
    fun observeBySite(siteId: String): LiveData<List<SiteTaskEntity>>

    @Query("UPDATE site_tasks SET isCompleted = :completed, completedAt = :completedAt, synced = 0 WHERE taskId = :taskId")
    suspend fun setCompleted(taskId: String, completed: Boolean, completedAt: Long?)
}
