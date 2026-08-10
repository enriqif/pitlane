package com.widoo.pitlane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.ui.navigation.AppNavigation
import com.widoo.pitlane.ui.navigation.Screen
import com.widoo.pitlane.ui.screen.SplashScreen
import com.widoo.pitlane.ui.screen.onboarding.OnboardingScreen
import com.widoo.pitlane.ui.theme.PitlaneTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val preferencesManager: PreferencesManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Detectar si viene del widget
        val openFuel = intent?.action == "ACTION_OPEN_FUEL"

        // Leer el valor inicial de forma síncrona antes de setContent
        var initialOnboardingValue: Boolean? = null
        lifecycleScope.launch {
            initialOnboardingValue = preferencesManager
                .isOnboardingCompleted
                .first()
        }

        setContent {
            PitlaneTheme {
                val onboardingCompleted by preferencesManager
                    .isOnboardingCompleted
                    .collectAsState(initial = initialOnboardingValue)

                when (onboardingCompleted) {
                    null -> SplashScreen()           // ← mientras carga DataStore
                    true -> AppNavigation(
                        startDestination = if (openFuel)
                            Screen.Fuel.route
                        else
                            Screen.Home.route
                    )
                    false -> OnboardingScreen(
                        onComplete = { recreate() }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting("TEST")
}