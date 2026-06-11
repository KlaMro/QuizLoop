package com.example.quizloop.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(), // Placeholder, will be injected properly later
    onContinueAsGuest: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegistering) "Create Account" else "Welcome to QuizLoop!",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is AuthState.Success -> {
                    Text(state.message, color = MaterialTheme.colorScheme.primary)
                    // Navigate on successful login
                    if (state.message.contains("Login successful")) {
                        onLoginSuccess()
                    }
                }
                else -> Unit
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isRegistering) {
                        authViewModel.register(email, password)
                    } else {
                        authViewModel.login(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState != AuthState.Loading
            ) {
                Text(if (isRegistering) "Register" else "Login")
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { isRegistering = !isRegistering }) {
                Text(if (isRegistering) "Already have an account? Login" else "Don't have an account? Register")
            }

            if (!isRegistering) {
                TextButton(onClick = { authViewModel.sendPasswordReset(email) }) {
                    Text("Forgot Password?")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onContinueAsGuest, modifier = Modifier.fillMaxWidth()) {
                Text("Continue as Guest")
            }
        }
    }
}
