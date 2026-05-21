package com.pl.pulselab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pl.pulselab.presentation.auth.AuthUiState
import com.pl.pulselab.presentation.auth.AuthViewModel
import com.pl.pulselab.presentation.auth.SignInScreen
import com.pl.pulselab.presentation.auth.SignInViewModel
import com.pl.pulselab.presentation.status.StatusViewModel
import com.pl.pulselab.ui.theme.PulseLabTheme

private val Navy = Color(0xFF0A1628)
private val Teal = Color(0xFF00BFA5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PulseLabTheme {
                val authViewModel: AuthViewModel = viewModel()
                val signInViewModel: SignInViewModel = viewModel()
                val statusViewModel: StatusViewModel = viewModel()
                val authState by authViewModel.uiState.collectAsState()

                when (authState) {
                    is AuthUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Navy),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Teal)
                        }
                    }
                    is AuthUiState.SignedOut -> {
                        SignInScreen(viewModel = signInViewModel)
                    }
                    is AuthUiState.SignedIn -> {
                        StatusScreen(
                            viewModel = statusViewModel,
                            onSignOut = { authViewModel.signOut() }
                        )
                    }
                }
            }
        }
    }
}
