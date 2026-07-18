package com.widoo.pitlane.data.repository

import com.widoo.pitlane.data.local.dao.VehicleDao
import com.widoo.pitlane.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val dao: VehicleDao) {
    fun getAll(): Flow<List<VehicleEntity>> = dao.getAll()
    fun getActive(): Flow<VehicleEntity?> = dao.getActive()
    suspend fun getCount(): Int = dao.getCount()
    suspend fun insert(vehicle: VehicleEntity): Long = dao.insert(vehicle)
    suspend fun update(vehicle: VehicleEntity) = dao.update(vehicle)
    suspend fun setActive(id: Long) {
        dao.clearActive()
        dao.setActive(id)
    }
    suspend fun updateKm(id: Long, km: Int) = dao.updateKm(id, km)
    suspend fun delete(vehicle: VehicleEntity) = dao.delete(vehicle)
}