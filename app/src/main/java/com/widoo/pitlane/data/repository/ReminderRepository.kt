package com.widoo.pitlane.data.repository

import com.widoo.pitlane.data.local.dao.ReminderDao
import com.widoo.pitlane.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun getPending(): Flow<List<ReminderEntity>> = dao.getPending()
    fun getAll(): Flow<List<ReminderEntity>> = dao.getAll()
    suspend fun insert(reminder: ReminderEntity): Long = dao.insert(reminder)
    suspend fun update(reminder: ReminderEntity) = dao.update(reminder)
    suspend fun delete(reminder: ReminderEntity) = dao.delete(reminder)
}