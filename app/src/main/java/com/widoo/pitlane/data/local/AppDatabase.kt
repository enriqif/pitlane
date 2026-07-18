package com.widoo.pitlane.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.widoo.pitlane.data.local.dao.FuelLogDao
import com.widoo.pitlane.data.local.dao.ReminderDao
import com.widoo.pitlane.data.local.dao.ServiceRecordDao
import com.widoo.pitlane.data.local.dao.VehicleDao
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.ReminderEntity
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        ServiceRecordEntity::class,
        FuelLogEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun reminderDao(): ReminderDao
}