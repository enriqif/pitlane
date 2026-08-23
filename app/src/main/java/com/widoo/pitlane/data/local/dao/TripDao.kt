package com.widoo.pitlane.data.local.dao

import androidx.room.*
import com.widoo.pitlane.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE status = 'PENDING' ORDER BY endedAt DESC LIMIT 1")
    fun getPending(): Flow<TripEntity?>

    @Insert
    suspend fun insert(trip: TripEntity): Long

    @Update
    suspend fun update(trip: TripEntity)
}
