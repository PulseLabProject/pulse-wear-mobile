package com.pl.pulselab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.pulselab.presentation.status.StatusViewModel
import com.pl.pulselab.ui.theme.PulseLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PulseLabTheme {
                val viewModel: StatusViewModel = viewModel()
                StatusScreen(viewModel = viewModel)
            }
        }
    }
}
