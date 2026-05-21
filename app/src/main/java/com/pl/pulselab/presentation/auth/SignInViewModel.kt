package com.pl.pulselab.presentation.auth

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

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
        _emailError.value = null
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        _passwordError.value = null
        if (_uiState.value is SignInUiState.Error) {
            _uiState.value = SignInUiState.Idle
        }
    }

    fun validate(): Boolean {
        val emailVal = _email.value
        val passwordVal = _password.value
        var valid = true
        when {
            emailVal.isBlank() -> {
                _emailError.value = "Email is required"
                valid = false
            }
            !emailVal.contains('@') -> {
                _emailError.value = "Enter a valid email address"
                valid = false
            }
            else -> _emailError.value = null
        }
        if (passwordVal.isBlank()) {
            _passwordError.value = "Password is required"
            valid = false
        } else {
            _passwordError.value = null
        }
        return valid
    }

    fun signInWithPassword() {
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
