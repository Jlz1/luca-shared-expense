package com.luca.shared.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UI State for Forgot Password flow
 */
sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    object EmailSent : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

/**
 * ViewModel for handling Forgot Password / Reset Password logic
 * with Firebase Authentication.
 */
class ForgotPasswordViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- STATE FLOWS ---
    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    companion object {
        private const val TAG = "ForgotPasswordVM"
    }

    /**
     * Send password reset email using standard Firebase method.
     * User will reset password via Firebase's web page, then return to app to login.
     */
    fun sendPasswordResetEmail(email: String) {
        val cleanEmail = email.trim().lowercase()

        if (cleanEmail.isBlank()) {
            _uiState.value = ForgotPasswordUiState.Error("Email tidak boleh kosong")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = ForgotPasswordUiState.Error("Format email tidak valid")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ForgotPasswordUiState.Loading

            try {
                // Use simple sendPasswordResetEmail without ActionCodeSettings
                // This sends the standard Firebase password reset email
                // which works reliably without requiring Dynamic Links setup
                auth.sendPasswordResetEmail(cleanEmail).await()

                _email.value = cleanEmail
                _uiState.value = ForgotPasswordUiState.EmailSent
                Log.d(TAG, "Password reset email sent to: $cleanEmail")

            } catch (e: FirebaseAuthInvalidUserException) {
                Log.e(TAG, "No user found with email: $cleanEmail", e)
                _uiState.value = ForgotPasswordUiState.Error("Akun dengan email ini tidak ditemukan")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, "Invalid email format", e)
                _uiState.value = ForgotPasswordUiState.Error("Format email tidak valid")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send reset email", e)
                _uiState.value = ForgotPasswordUiState.Error(
                    e.message ?: "Gagal mengirim email reset password"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset UI state to idle
     */
    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
        _isLoading.value = false
    }

    /**
     * Clear all data (for cleanup)
     */
    fun clearData() {
        _uiState.value = ForgotPasswordUiState.Idle
        _isLoading.value = false
        _email.value = ""
    }
}
