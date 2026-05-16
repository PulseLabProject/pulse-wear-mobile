package com.pl.pulselab.presentation.signin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text

@Composable
fun SignInScreen(viewModel: SignInViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    SignInScreenContent(
        uiState = uiState,
        email = email,
        password = password,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onSignIn = { viewModel.signIn(email, password) }
    )
}

@Composable
fun SignInScreenContent(
    uiState: SignInUiState,
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit
) {
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
                    BasicTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        decorationBox = { inner ->
                            if (email.isEmpty()) Text("Email", style = MaterialTheme.typography.bodyMedium)
                            inner()
                        }
                    )
                }

                item {
                    BasicTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        visualTransformation = PasswordVisualTransformation(),
                        decorationBox = { inner ->
                            if (password.isEmpty()) Text("Password", style = MaterialTheme.typography.bodyMedium)
                            inner()
                        }
                    )
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
                    if (uiState is SignInUiState.Loading) {
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
            }
        }
    }
}
