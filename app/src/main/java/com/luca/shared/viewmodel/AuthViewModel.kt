package com.luca.shared.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luca.shared.data.AuthRepository
import com.luca.shared.data.repository.EmailRepository
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
        auth.createUserWithEmailAndPassword(tempEmail, tempPassword)
            .addOnSuccessListener { authResult ->
                // Ambil UID langsung dari hasil, tanpa bergantung pada currentUser
                val uid = authResult.user?.uid
                if (uid == null) {
                    isLoading = false
                    otpVerificationStatus = null
                    errorMessage = "Akun berhasil dibuat, tetapi UID tidak tersedia. Coba lagi."
                    return@addOnSuccessListener
                }

                // Simpan ke Firestore menggunakan UID (menghindari duplikasi by email)
                saveUserToFirestore(uid, tempName, tempEmail)

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
    private fun saveUserToFirestore(uid: String, name: String, email: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userData = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to name,
            // placeholder avatar, will be updated in Fill Profile
            "avatarName" to "avatar_1",
            "createdAt" to System.currentTimeMillis()
        )
        // Gunakan UID sebagai document ID agar konsisten dengan flow lain, dan merge agar aman
        db.collection("users").document(uid).set(userData, com.google.firebase.firestore.SetOptions.merge())
    }

    // =================================================================
    //  BAGIAN 2: LOGIKA LAMA (Login, Google)
    // =================================================================

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val cleanEmail = email.trim().lowercase()
            val result = authRepository.loginManual(cleanEmail, pass)

            result.onSuccess { exists ->
                // Only navigate when Firestore user document exists
                if (exists) {
                    isSuccess = true
                } else {
                    // Treat as a unified case: account does not exist (deleted or never created)
                    errorMessage = "Account does not exist"
                }
            }.onFailure { error ->
                // Preserve specific auth credential errors from Firebase
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

    private fun handleSocialResult(result: Result<Pair<Boolean, Boolean>>) {
        result.onSuccess { (_, isNew) ->
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