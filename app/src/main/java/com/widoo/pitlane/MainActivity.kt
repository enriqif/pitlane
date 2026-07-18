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
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.ui.navigation.AppNavigation
import com.widoo.pitlane.ui.screen.onboarding.OnboardingScreen
import com.widoo.pitlane.ui.theme.PitlaneTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val preferencesManager: PreferencesManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PitlaneTheme {
                val onboardingCompleted by preferencesManager
                    .isOnboardingCompleted
                    .collectAsState(initial = false)

                if (onboardingCompleted) {
                    AppNavigation()
                } else {
                    OnboardingScreen(
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