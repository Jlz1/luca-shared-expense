package com.example.luca.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // State untuk UI
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    // --- GOOGLE LOGIN ---
    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // Sekarang ini valid karena signInWithGoogle mengembalikan Result<String>
            val result = repository.signInWithGoogle(idToken)

            result.onSuccess {
                isSuccess = true
            }.onFailure { error ->
                errorMessage = error.message ?: "Google Sign In Gagal"
            }
            isLoading = false
        }
    }

    // --- REGISTER MANUAL ---
    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.registerManual(email, pass, name)

            result.onSuccess {
                isSuccess = true
            }.onFailure { error ->
                errorMessage = error.message ?: "Registrasi Gagal"
            }
            isLoading = false
        }
    }

    // --- LOGIN MANUAL ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.loginManual(email, pass)

            result.onSuccess {
                isSuccess = true
            }.onFailure { error ->
                errorMessage = error.message ?: "Login failed"
            }
            isLoading = false
        }
    }

    // Reset state agar tidak loop saat navigasi
    fun resetState() {
        isSuccess = false
        errorMessage = null
        isLoading = false
    }
}