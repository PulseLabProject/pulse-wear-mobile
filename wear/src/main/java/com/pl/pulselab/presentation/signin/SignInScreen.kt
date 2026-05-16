package com.pl.pulselab.presentation.signin

import android.app.RemoteInput
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.remote.interactions.RemoteActivityHelper
import kotlinx.coroutines.delay

private const val KEY_EMAIL = "email_input"
private const val KEY_PASSWORD = "password_input"

@Composable
fun SignInScreen(viewModel: SignInViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val emailValue by viewModel.email.collectAsState()
    val passwordValue by viewModel.password.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    val emailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val bundle = RemoteInput.getResultsFromIntent(result.data)
        bundle?.getString(KEY_EMAIL)?.let { viewModel.onEmailChange(it) }
    }

    val passwordLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val bundle = RemoteInput.getResultsFromIntent(result.data)
        bundle?.getString(KEY_PASSWORD)?.let { viewModel.onPasswordChange(it) }
    }

    SignInScreenContent(
        uiState = uiState,
        emailValue = emailValue,
        passwordValue = passwordValue,
        emailError = emailError,
        passwordError = passwordError,
        onEmailChipClick = {
            val remoteInput = RemoteInput.Builder(KEY_EMAIL).setLabel("Enter email").build()
            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent() ?: return@SignInScreenContent
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
            emailLauncher.launch(intent)
        },
        onPasswordChipClick = {
            val remoteInput = RemoteInput.Builder(KEY_PASSWORD).setLabel("Enter password").build()
            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent() ?: return@SignInScreenContent
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
            passwordLauncher.launch(intent)
        },
        onSignIn = {
            if (viewModel.validate()) {
                viewModel.signIn()
            }
        }
    )
}

@Composable
fun SignInScreenContent(
    uiState: SignInUiState,
    emailValue: String,
    passwordValue: String,
    emailError: String?,
    passwordError: String?,
    onEmailChipClick: () -> Unit,
    onPasswordChipClick: () -> Unit,
    onSignIn: () -> Unit
) {
    val context = LocalContext.current
    var showPhoneError by remember { mutableStateOf(false) }

    if (showPhoneError) {
        LaunchedEffect(showPhoneError) {
            delay(3000)
            showPhoneError = false
        }
    }

    val isError = uiState is SignInUiState.Error
    val isLoading = uiState is SignInUiState.Loading
    val passwordChipColors = if (isError) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        ScreenScaffold(scrollState = listState) { contentPadding ->
            TransformingLazyColumn(contentPadding = contentPadding, state = listState) {

                item {
                    ListHeader(modifier = Modifier.fillMaxWidth()) {
                        Text("Sign In")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onEmailChipClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (emailValue.isEmpty()) "Email" else emailValue)
                    }
                }

                if (emailError != null) {
                    item {
                        Text(
                            text = emailError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onPasswordChipClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = passwordChipColors
                    ) {
                        Text(if (passwordValue.isEmpty()) "Password" else "•".repeat(passwordValue.length))
                    }
                }

                if (passwordError != null) {
                    item {
                        Text(
                            text = passwordError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                when (val state = uiState) {
                    is SignInUiState.Error -> {
                        item {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }

                item {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = onSignIn,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign In")
                        }
                    }
                }

                item {
                    CompactButton(
                        onClick = {
                            val intent = Intent("com.pl.pulselab.SIGN_IN")
                            val helper = RemoteActivityHelper(
                                context,
                                ContextCompat.getMainExecutor(context)
                            )
                            val future = try {
                                helper.startRemoteActivity(intent, null)
                            } catch (e: Exception) {
                                showPhoneError = true
                                null
                            }
                            future?.addListener({
                                try {
                                    future.get()
                                } catch (e: Exception) {
                                    showPhoneError = true
                                }
                            }, ContextCompat.getMainExecutor(context))
                        },
                        enabled = !isLoading
                    ) {
                        Text("Use phone")
                    }
                }

                if (showPhoneError) {
                    item {
                        Text(
                            text = "Phone not available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
