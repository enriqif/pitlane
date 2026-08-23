package com.widoo.pitlane.data.repository

import com.widoo.pitlane.data.local.dao.ReminderDao
import com.widoo.pitlane.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun getPending(): Flow<List<ReminderEntity>> = dao.getPending()

    fun getPendingByVehicle(vehicleId: Long): Flow<List<ReminderEntity>> =
        dao.getPendingByVehicle(vehicleId)

    fun getAllByVehicle(vehicleId: Long): Flow<List<ReminderEntity>> =
        dao.getAllByVehicle(vehicleId)

    fun getAll(): Flow<List<ReminderEntity>> = dao.getAll()

    suspend fun insert(reminder: ReminderEntity): Long = dao.insert(reminder)

    suspend fun update(reminder: ReminderEntity) =
        dao.update(reminder.copy(updatedAt = System.currentTimeMillis()))

    suspend fun delete(reminder: ReminderEntity) =
        dao.update(reminder.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
}