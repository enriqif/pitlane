package com.widoo.pitlane.data.repository

import com.widoo.pitlane.data.local.dao.TripDao
import com.widoo.pitlane.data.local.entity.TripEntity
import com.widoo.pitlane.data.local.entity.TripStatus
import kotlinx.coroutines.flow.Flow

class TripRepository(private val dao: TripDao) {
    fun getPending(): Flow<TripEntity?> = dao.getPending()

    suspend fun insert(trip: TripEntity): Long = dao.insert(trip)

    suspend fun setStatus(trip: TripEntity, status: String) =
        dao.update(trip.copy(status = status))

    suspend fun confirm(trip: TripEntity) = setStatus(trip, TripStatus.CONFIRMED)

    suspend fun discard(trip: TripEntity) = setStatus(trip, TripStatus.DISCARDED)
}
