package com.example.workassistance.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.workassistance.data.local.entity.SiteRequestEntity

@Dao
interface SiteRequestDao {

    @Insert
    suspend fun insert(request: SiteRequestEntity): Long

    @Query("SELECT * FROM site_requests WHERE siteId = :siteId ORDER BY createdAt DESC")
    fun observeBySite(siteId: String): LiveData<List<SiteRequestEntity>>
}
