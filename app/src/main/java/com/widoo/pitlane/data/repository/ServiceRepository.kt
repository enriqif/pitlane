package com.widoo.pitlane.data.repository

import com.widoo.pitlane.data.local.dao.ServiceRecordDao
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import kotlinx.coroutines.flow.Flow

class ServiceRepository(private val dao: ServiceRecordDao) {

    fun getAll(): Flow<List<ServiceRecordEntity>> = dao.getAll()

    fun getByVehicle(vehicleId: Long): Flow<List<ServiceRecordEntity>> =
        dao.getByVehicle(vehicleId)

    suspend fun getLatest(): ServiceRecordEntity? = dao.getLatest()

    suspend fun insert(record: ServiceRecordEntity): Long = dao.insert(record)

    suspend fun delete(record: ServiceRecordEntity) = dao.delete(record)
}