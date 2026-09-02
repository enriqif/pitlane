package com.widoo.pitlane.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pitlane_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // El usuario habilitó explícitamente que la app mida los viajes sola apenas
        // se conecta a Android Auto (y los finalice al desconectarse), sin tener que
        // tocar "Iniciar viaje". Requiere permiso de ubicación en segundo plano.
        val AUTO_TRIP_TRACKING = booleanPreferencesKey("auto_trip_tracking")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }

    val isAutoTripTrackingEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[AUTO_TRIP_TRACKING] ?: false }

    suspend fun setAutoTripTracking(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_TRIP_TRACKING] = enabled }
    }
}
