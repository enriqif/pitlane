package com.widoo.pitlane.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

/**
 * Migraciones reales (no destructivas) para no perder los datos de los usuarios de la
 * beta cada vez que se agrega/cambia un campo. Reemplazan a fallbackToDestructiveMigration.
 */

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Agrega la tabla de viajes (tracking de km por GPS)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `trips` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `vehicleId` INTEGER NOT NULL,
                `distanceMeters` REAL NOT NULL,
                `startedAt` INTEGER NOT NULL,
                `endedAt` INTEGER NOT NULL,
                `status` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Campos para la futura sincronización con la API
        val now = System.currentTimeMillis()

        db.execSQL("ALTER TABLE vehicles ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vehicles ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $now")
        db.execSQL("ALTER TABLE vehicles ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE service_records ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE service_records ADD COLUMN createdAt INTEGER NOT NULL DEFAULT $now")
        db.execSQL("ALTER TABLE service_records ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $now")
        db.execSQL("ALTER TABLE service_records ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE fuel_logs ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE fuel_logs ADD COLUMN createdAt INTEGER NOT NULL DEFAULT $now")
        db.execSQL("ALTER TABLE fuel_logs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $now")
        db.execSQL("ALTER TABLE fuel_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE reminders ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE reminders ADD COLUMN createdAt INTEGER NOT NULL DEFAULT $now")
        db.execSQL("ALTER TABLE reminders ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $now")
        db.execSQL("ALTER TABLE reminders ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

        // remoteId tiene que ser único por fila de verdad, no un '' compartido por todas
        fillRandomRemoteIds(db, "vehicles")
        fillRandomRemoteIds(db, "service_records")
        fillRandomRemoteIds(db, "fuel_logs")
        fillRandomRemoteIds(db, "reminders")
    }

    private fun fillRandomRemoteIds(db: SupportSQLiteDatabase, table: String) {
        val ids = mutableListOf<Long>()
        db.query("SELECT id FROM $table").use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0))
            }
        }
        ids.forEach { id ->
            db.execSQL(
                "UPDATE $table SET remoteId = ? WHERE id = ?",
                arrayOf(UUID.randomUUID().toString(), id)
            )
        }
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Kilometraje previo del vehículo, para poder deshacer un viaje aplicado por GPS
        db.execSQL("ALTER TABLE trips ADD COLUMN previousKm INTEGER NOT NULL DEFAULT 0")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
