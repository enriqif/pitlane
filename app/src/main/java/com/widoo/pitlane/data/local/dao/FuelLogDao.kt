package com.widoo.pitlane.data.local.dao

import androidx.room.*
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {
    @Query("SELECT * FROM fuel_logs WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAll(): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_logs WHERE isDeleted = 0 ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): FuelLogEntity?

    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId AND isDeleted = 0 ORDER BY date DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<FuelLogEntity>>

    @Query("SELECT SUM(totalCost) FROM fuel_logs WHERE vehicleId = :vehicleId AND date >= :fromDate AND isDeleted = 0")
    suspend fun getTotalCostSince(vehicleId: Long, fromDate: Long): Double?

    @Query("SELECT SUM(liters) FROM fuel_logs WHERE vehicleId = :vehicleId AND date >= :fromDate AND isDeleted = 0")
    suspend fun getTotalLitersSince(vehicleId: Long, fromDate: Long): Double?

    @Query("SELECT SUM(totalCost) FROM fuel_logs WHERE date >= :fromDate AND isDeleted = 0")
    suspend fun getTotalCostSince(fromDate: Long): Double?

    @Query("SELECT SUM(liters) FROM fuel_logs WHERE date >= :fromDate AND isDeleted = 0")
    suspend fun getTotalLitersSince(fromDate: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: FuelLogEntity): Long

    @Update
    suspend fun update(log: FuelLogEntity)
}
