package com.pl.pulselab.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.pl.pulselab.presentation.home.HomeScreen
import com.pl.pulselab.presentation.theme.PulseLabTheme

// MainActivity is intentionally minimal — its only job is to bootstrap the app.
// All screen logic lives in HomeScreen + HomeViewModel.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show the splash screen defined in res/drawable/splash_icon.xml while the app loads
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            PulseLabTheme {
                // Entry point of the UI. Swap HomeScreen() for a NavHost here
                // when you add multiple screens later.
                HomeScreen()
            }
        }
    }
}