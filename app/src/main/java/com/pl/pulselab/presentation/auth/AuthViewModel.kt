package com.pl.pulselab.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerk.api.Clerk
import com.clerk.api.session.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Loading : AuthUiState()
    object SignedOut : AuthUiState()
    object SignedIn : AuthUiState()
}

class AuthViewModel : ViewModel() {

    val uiState: StateFlow<AuthUiState> = combine(
        Clerk.isInitialized,
        Clerk.sessionFlow
    ) { initialized: Boolean?, session: Session? ->
        when {
            initialized != true -> AuthUiState.Loading
            session == null -> AuthUiState.SignedOut
            else -> AuthUiState.SignedIn
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthUiState.Loading
    )

    fun signOut() {
        viewModelScope.launch { Clerk.auth.signOut() }
    }
}
