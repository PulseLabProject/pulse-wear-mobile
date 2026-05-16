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

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun signIn() {
        val emailVal = _email.value
        val passwordVal = _password.value
        _uiState.value = SignInUiState.Loading
        viewModelScope.launch {
            val result = Clerk.auth.signInWithPassword {
                identifier = emailVal
                this.password = passwordVal
            }
            when (result) {
                is ClerkResult.Success -> {
                    _uiState.value = SignInUiState.Idle
                }
                is ClerkResult.Failure -> {
                    _password.value = ""
                    val msg = result.error?.errors?.firstOrNull()?.message
                        ?: result.throwable?.message
                        ?: "Sign in failed"
                    _uiState.value = SignInUiState.Error(msg)
                }
            }
        }
    }
}
