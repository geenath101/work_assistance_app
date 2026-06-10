package com.example.workassistance.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.workassistance.data.local.entity.ProofOfWorkPhoto

@Dao
interface ProofOfWorkDao {

    @Insert
    suspend fun insert(photo: ProofOfWorkPhoto): Long

    @Query("SELECT * FROM proof_of_work_photos WHERE siteId = :siteId ORDER BY createdAt DESC")
    fun observeBySite(siteId: String): LiveData<List<ProofOfWorkPhoto>>

    @Query("SELECT * FROM proof_of_work_photos WHERE siteId = :siteId AND taskId = :taskId ORDER BY createdAt DESC")
    fun observeBySiteTask(siteId: String, taskId: String): LiveData<List<ProofOfWorkPhoto>>

    @Query("SELECT COUNT(*) FROM proof_of_work_photos WHERE siteId = :siteId AND taskId = :taskId")
    suspend fun countBySiteTask(siteId: String, taskId: String): Int

    @Query("SELECT uri FROM proof_of_work_photos WHERE siteId = :siteId AND taskId = :taskId ORDER BY createdAt DESC")
    suspend fun getUrisBySiteTask(siteId: String, taskId: String): List<String>

    @Query("DELETE FROM proof_of_work_photos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
