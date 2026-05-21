package com.pl.pulselab.presentation.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Navy = Color(0xFF0A1628)
private val Teal = Color(0xFF00BFA5)
private val TextPrimary = Color(0xFFF0F4F8)
private val TextSecondary = Color(0xFF8A9BB0)
private val NavyLight = Color(0xFF152238)
private val ErrorRed = Color(0xFFEF5350)

@Composable
fun SignInScreen(viewModel: SignInViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val context = LocalContext.current
    SignInScreenContent(
        uiState = uiState,
        email = email,
        password = password,
        emailError = emailError,
        passwordError = passwordError,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = { if (viewModel.validate()) viewModel.signInWithPassword() },
        onSignInWithGoogle = { viewModel.signInWithGoogle(context as Activity) }
    )
}

@Composable
fun SignInScreenContent(
    uiState: SignInUiState,
    email: String,
    password: String,
    emailError: String?,
    passwordError: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignInWithGoogle: () -> Unit
) {
    val isLoading = uiState is SignInUiState.Loading
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = NavyLight,
        unfocusedContainerColor = NavyLight,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedIndicatorColor = Teal,
        unfocusedIndicatorColor = TextSecondary,
        cursorColor = Teal,
        focusedLabelColor = Teal,
        unfocusedLabelColor = TextSecondary
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "PULSELAB",
                color = Teal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Sign In",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(Modifier.height(32.dp))

            if (uiState is SignInUiState.Error) {
                Text(
                    uiState.message,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            TextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            if (emailError != null) {
                Text(
                    emailError,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            if (passwordError != null) {
                Text(
                    passwordError,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal)
                }
            } else {
                Button(
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("Sign In", color = Navy, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = TextSecondary)
                    Text(
                        "  or  ",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = TextSecondary)
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onSignInWithGoogle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with Google", color = TextPrimary)
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SignInScreenPreview() {
    SignInScreenContent(
        uiState = SignInUiState.Idle,
        email = "",
        password = "",
        emailError = null,
        passwordError = null,
        onEmailChange = {},
        onPasswordChange = {},
        onSignIn = {},
        onSignInWithGoogle = {}
    )
}

@Preview(showSystemUi = true)
@Composable
fun SignInScreenErrorPreview() {
    SignInScreenContent(
        uiState = SignInUiState.Error("Invalid credentials"),
        email = "test@example.com",
        password = "",
        emailError = null,
        passwordError = "Password is required",
        onEmailChange = {},
        onPasswordChange = {},
        onSignIn = {},
        onSignInWithGoogle = {}
    )
}
