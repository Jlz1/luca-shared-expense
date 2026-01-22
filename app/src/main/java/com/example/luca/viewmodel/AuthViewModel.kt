package com.example.luca.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.AuthRepository
import com.example.luca.data.repository.EmailRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.random.Random

class AuthViewModel : ViewModel() {

    // Repository Lama (untuk Login & Google)
    private val authRepository = AuthRepository()
    // Repository Baru (khusus kirim Email)
    private val emailRepository = EmailRepository()

    // Instance Firebase Auth (untuk create account manual)
    private val auth = FirebaseAuth.getInstance()

    // --- STATE UI ---
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Navigasi
    var isSuccess by mutableStateOf(false) // Masuk Home
    var isNewUser by mutableStateOf(false) // Masuk Fill Profile

    // --- STATE OTP (BARU) ---
    var otpSentStatus by mutableStateOf(false) // True = Email terkirim, pindah ke layar OTP
    var otpVerificationStatus by mutableStateOf<Boolean?>(null) // True = Kode Cocok

    // Penyimpanan Sementara (Data user ditahan dulu sampai OTP benar)
    private var tempName = ""
    private var tempEmail = ""
    private var tempPassword = ""
    private var generatedOtpCode = ""

    // =================================================================
    //  BAGIAN 1: LOGIKA OTP (Generate -> Kirim -> Verifikasi -> Buat Akun)
    // =================================================================

    // Tahap 1: User klik Sign Up -> Generate Kode & Kirim Email
    fun startSignUpProcess(name: String, email: String, pass: String) {
        val cleanEmail = email.trim().lowercase()

        if (name.isBlank() || cleanEmail.isBlank() || pass.isBlank()) {
            errorMessage = "Semua data harus diisi"
            return
        }

        isLoading = true
        errorMessage = null

        // 1. Generate Angka Acak 4 Digit
        val otp = Random.nextInt(1000, 9999).toString()
        generatedOtpCode = otp

        // 2. Simpan data di memori sementara
        tempName = name
        tempEmail = cleanEmail
        tempPassword = pass

        Log.d("AuthViewModel", "Kode OTP untuk $cleanEmail: $otp") // Cek Logcat kalau mau intip kode

        // 3. Kirim Email via Repository
        viewModelScope.launch {
            val success = emailRepository.sendOtpToEmail(cleanEmail, name, otp)
            isLoading = false

            if (success) {
                otpSentStatus = true // Trigger UI pindah ke OtpScreen
            } else {
                errorMessage = "Gagal kirim email. Cek koneksi internet."
            }
        }
    }

    // Tahap 2: User masukkan kode di OtpScreen -> Cek Kesamaan
    fun verifyOtp(inputCode: String) {
        if (inputCode == generatedOtpCode) {
            // Kode Cocok! -> Lanjut bikin akun di Firebase
            otpVerificationStatus = true
            createFirebaseAccount()
        } else {
            // Kode Salah
            otpVerificationStatus = false
        }
    }

    // Tahap 3: Bikin Akun (Hanya dipanggil kalau OTP benar)
    private fun createFirebaseAccount() {
        isLoading = true
        // Kita pakai fungsi manual di sini biar lebih terkontrol flow-nya
        auth.createUserWithEmailAndPassword(tempEmail, tempPassword)
            .addOnSuccessListener {
                // Sukses Bikin Akun -> Simpan Nama ke Database
                // Kita pinjam fungsi updateProfile dari AuthRepository kalau ada,
                // atau panggil langsung fungsi helper di bawah.
                saveUserToFirestore(tempName, tempEmail)

                isNewUser = true // Arahkan ke Fill Profile
                isLoading = false
            }
            .addOnFailureListener { e ->
                isLoading = false
                otpVerificationStatus = null
                errorMessage = "Gagal membuat akun: ${e.message}"
            }
    }

    // Helper Simpan ke Firestore (Versi Cepat)
    private fun saveUserToFirestore(name: String, email: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val user = hashMapOf("name" to name, "email" to email)
        db.collection("users").document(email).set(user)
    }

    // =================================================================
    //  BAGIAN 2: LOGIKA LAMA (Login, Google, Twitter) - TETAP ADA
    // =================================================================

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val cleanEmail = email.trim().lowercase()
            val result = authRepository.loginManual(cleanEmail, pass)

            result.onSuccess {
                isSuccess = true
            }.onFailure { error ->
                errorMessage = error.message
            }
            isLoading = false
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = authRepository.signInWithGoogle(idToken)
            handleSocialResult(result)
        }
    }

    fun twitterLogin(activity: Activity) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = authRepository.signInWithTwitter(activity)
            handleSocialResult(result)
        }
    }

    private fun handleSocialResult(result: Result<Pair<Boolean, Boolean>>) {
        result.onSuccess { (success, isNew) ->
            if (isNew) isNewUser = true else isSuccess = true
        }.onFailure {
            errorMessage = it.message
        }
        isLoading = false
    }

    // Reset State (Penting saat pindah halaman)
    fun resetState() {
        isSuccess = false
        isNewUser = false
        otpSentStatus = false
        otpVerificationStatus = null
        errorMessage = null
        isLoading = false
    }

    fun resetOtpStatus() {
        otpVerificationStatus = null
    }
}