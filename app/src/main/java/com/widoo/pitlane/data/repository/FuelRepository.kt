package com.widoo.pitlane.data.repository

import com.widoo.pitlane.data.local.dao.FuelLogDao
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import kotlinx.coroutines.flow.Flow

class FuelRepository(private val dao: FuelLogDao) {
    fun getAll(): Flow<List<FuelLogEntity>> = dao.getAll()
    suspend fun getLatest(): FuelLogEntity? = dao.getLatest()
    suspend fun getTotalCostSince(fromDate: Long): Double? = dao.getTotalCostSince(fromDate)
    suspend fun getTotalLitersSince(fromDate: Long): Double? = dao.getTotalLitersSince(fromDate)
    suspend fun insert(log: FuelLogEntity): Long = dao.insert(log)
    suspend fun delete(log: FuelLogEntity) = dao.delete(log)
}