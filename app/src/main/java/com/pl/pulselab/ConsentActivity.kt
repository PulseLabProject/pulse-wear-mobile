package com.pl.pulselab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pl.pulselab.ui.theme.PulseLabTheme

class ConsentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PulseLabTheme {
                DataConsentScreen(
                    onAccept = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onDecline = {
                        finishAffinity()
                    }
                )
            }
        }
    }
}
