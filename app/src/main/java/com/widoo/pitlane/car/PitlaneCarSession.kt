package com.widoo.pitlane.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class PitlaneCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = TripCarScreen(carContext)
}
