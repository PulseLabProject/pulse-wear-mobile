package com.pl.pulselab.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.ClerkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignInUiState {
    object Idle : SignInUiState()
    object Loading : SignInUiState()
    data class Error(val message: String) : SignInUiState()
}

class SignInViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        _uiState.value = SignInUiState.Loading
        viewModelScope.launch {
            val result = Clerk.auth.signInWithPassword {
                identifier = email
                this.password = password
            }
            when (result) {
                is ClerkResult.Success -> {
                    // Session flow in AuthViewModel picks up the change automatically.
                    _uiState.value = SignInUiState.Idle
                }
                is ClerkResult.Failure -> {
                    val msg = result.error?.errors?.firstOrNull()?.message
                        ?: result.throwable?.message
                        ?: "Sign in failed"
                    _uiState.value = SignInUiState.Error(msg)
                }
            }
        }
    }
}
