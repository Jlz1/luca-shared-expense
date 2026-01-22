package com.example.luca.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // --- STATE UI ---
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Navigasi ke Home
    var isSuccess by mutableStateOf(false)
        private set

    // Navigasi ke Fill Profile (Khusus Google/X User Baru/Hantu)
    var isNewUser by mutableStateOf(false)
        private set

    // --- 1. LOGIN MANUAL (Dipakai LoginScreen) ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // AUTO LOWERCASE EMAIL
            val cleanEmail = email.trim().lowercase()

            val result = repository.loginManual(cleanEmail, pass)

            result.onSuccess {
                isSuccess = true // LoginScreen akan baca ini & pindah ke Home
            }.onFailure { error ->
                errorMessage = error.message
            }
            isLoading = false
        }
    }

    // --- 2. GOOGLE LOGIN ---
    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.signInWithGoogle(idToken)
            handleSocialResult(result)
        }
    }

    // --- 3. TWITTER LOGIN ---
    fun twitterLogin(activity: Activity) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.signInWithTwitter(activity)
            handleSocialResult(result)
        }
    }

    // Helper untuk handle hasil social login
    private fun handleSocialResult(result: Result<Pair<Boolean, Boolean>>) {
        result.onSuccess { (success, isNew) ->
            if (isNew) {
                isNewUser = true // Arahkan ke Fill Profile
            } else {
                isSuccess = true // Arahkan ke Home
            }
        }.onFailure {
            errorMessage = it.message
        }
        isLoading = false
    }

    // --- 4. UPDATE PROFILE (Dipakai FillProfileScreen) ---
    fun updateProfile(username: String, avatarName: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.updateProfile(username, avatarName)
            result.onSuccess {
                isSuccess = true // Setelah isi profil, langsung ke Home
            }.onFailure {
                errorMessage = it.message
            }
            isLoading = false
        }
    }

    fun resetState() {
        isSuccess = false
        isNewUser = false
        errorMessage = null
        isLoading = false
    }
}