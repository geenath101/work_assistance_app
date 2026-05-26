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

    @Query("DELETE FROM proof_of_work_photos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
