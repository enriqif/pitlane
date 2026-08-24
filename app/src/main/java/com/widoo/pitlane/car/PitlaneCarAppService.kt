package com.widoo.pitlane.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class PitlaneCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator =
        // Para producción endurecida conviene restringir a los hosts oficiales
        // (Google Play Store / Android Auto / Android Automotive) en vez de aceptar todos.
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = PitlaneCarSession()
}
