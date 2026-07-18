package com.widoo.pitlane.data.local.dao

import androidx.room.*
import com.widoo.pitlane.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY createdAt ASC")
    fun getAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isActive = 1 LIMIT 1")
    fun getActive(): Flow<VehicleEntity?>

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE vehicles SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Long)

    @Query("UPDATE vehicles SET currentKm = :km WHERE id = :id")
    suspend fun updateKm(id: Long, km: Int)

    @Delete
    suspend fun delete(vehicle: VehicleEntity)
}