package com.widoo.pitlane.data.local.dao

import androidx.room.*
import com.widoo.pitlane.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND isDeleted = 0 ORDER BY triggerDate ASC")
    fun getPending(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId AND isCompleted = 0 AND isDeleted = 0 ORDER BY triggerDate ASC")
    fun getPendingByVehicle(vehicleId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId AND isDeleted = 0 ORDER BY triggerDate ASC")
    fun getAllByVehicle(vehicleId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isDeleted = 0 ORDER BY triggerDate ASC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)
}
