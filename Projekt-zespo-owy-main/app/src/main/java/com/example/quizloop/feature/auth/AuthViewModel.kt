package com.example.quizloop.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizloop.core.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.registerUser(email, pass)
                authRepository.sendVerificationEmail()
                _authState.value = AuthState.Success("Registration successful! Please check your email for verification.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.loginUser(email, pass)
                if (authRepository.getCurrentUser()?.isEmailVerified == true) {
                    _authState.value = AuthState.Success("Login successful!")
                } else {
                    _authState.value = AuthState.Error("Please verify your email before logging in.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.sendPasswordResetEmail(email)
                _authState.value = AuthState.Success("Password reset email sent! Check your inbox.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
