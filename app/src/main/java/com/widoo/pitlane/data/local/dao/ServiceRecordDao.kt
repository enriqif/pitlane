package com.widoo.pitlane.data.local.dao

import androidx.room.*
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRecordDao {
    @Query("SELECT * FROM service_records WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAll(): Flow<List<ServiceRecordEntity>>

    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId AND isDeleted = 0 ORDER BY date DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<ServiceRecordEntity>>

    @Query("SELECT * FROM service_records WHERE isDeleted = 0 ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): ServiceRecordEntity?

    @Query("SELECT * FROM service_records WHERE date >= :fromDate AND isDeleted = 0 ORDER BY date DESC")
    fun getFrom(fromDate: Long): Flow<List<ServiceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ServiceRecordEntity): Long

    @Update
    suspend fun update(record: ServiceRecordEntity)
}
