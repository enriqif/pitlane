package com.widoo.pitlane.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TripTrackingState(
    val isTracking: Boolean = false,
    val distanceMeters: Double = 0.0,
    val startedAt: Long = 0L
)

/**
 * Estado en vivo del viaje que está trackeando [TripTrackingService], compartido con la UI
 * mientras el service corre en background.
 */
object TripTracker {
    private val _state = MutableStateFlow(TripTrackingState())
    val state: StateFlow<TripTrackingState> = _state.asStateFlow()

    fun start(startedAt: Long) {
        _state.value = TripTrackingState(isTracking = true, startedAt = startedAt)
    }

    fun addDistance(meters: Double) {
        _state.value = _state.value.copy(distanceMeters = _state.value.distanceMeters + meters)
    }

    fun stop() {
        _state.value = TripTrackingState()
    }
}
