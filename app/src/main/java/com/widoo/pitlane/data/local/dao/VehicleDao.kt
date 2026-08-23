package com.widoo.pitlane.data.local.dao

import androidx.room.*
import com.widoo.pitlane.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE isDeleted = 0 ORDER BY createdAt ASC")
    fun getAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isActive = 1 AND isDeleted = 0 LIMIT 1")
    fun getActive(): Flow<VehicleEntity?>

    @Query("SELECT COUNT(*) FROM vehicles WHERE isDeleted = 0")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE vehicles SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Long)

    @Query("UPDATE vehicles SET currentKm = :km, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateKm(id: Long, km: Int, updatedAt: Long)

    @Transaction
    suspend fun switchActive(newActiveId: Long) {
        clearActive()
        setActive(newActiveId)
    }
}
