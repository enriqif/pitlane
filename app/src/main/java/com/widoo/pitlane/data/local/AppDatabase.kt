package com.widoo.pitlane.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.widoo.pitlane.data.local.dao.FuelLogDao
import com.widoo.pitlane.data.local.dao.ReminderDao
import com.widoo.pitlane.data.local.dao.ServiceRecordDao
import com.widoo.pitlane.data.local.dao.TripDao
import com.widoo.pitlane.data.local.dao.VehicleDao
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.ReminderEntity
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.data.local.entity.TripEntity
import com.widoo.pitlane.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        ServiceRecordEntity::class,
        FuelLogEntity::class,
        ReminderEntity::class,
        TripEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun reminderDao(): ReminderDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pitlane.db"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}