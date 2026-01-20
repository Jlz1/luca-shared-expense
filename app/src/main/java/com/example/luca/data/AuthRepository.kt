package com.example.luca.data

import com.example.luca.model.User // Pastikan import model User kamu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- 1. GOOGLE LOGIN (Diperbaiki return type-nya) ---
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()

            val user = result.user
            if (user != null) {
                // Cek apakah user baru, jika iya simpan ke Firestore
                val isNewUser = result.additionalUserInfo?.isNewUser == true
                if (isNewUser) {
                    saveUserToFirestore(user.uid, user.email ?: "", user.displayName ?: "")
                }
                Result.success("Login Google Berhasil")
            } else {
                Result.failure(Exception("Gagal login: User null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2. REGISTER MANUAL ---
    suspend fun registerManual(email: String, pass: String, name: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                saveUserToFirestore(user.uid, email, name)
                Result.success("Registrasi Berhasil")
            } else {
                Result.failure(Exception("Gagal membuat user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 3. LOGIN MANUAL ---
    suspend fun loginManual(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Login Berhasil")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DATABASE HELPER ---
    private suspend fun saveUserToFirestore(uid: String, email: String, name: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to name,
            "createdAt" to System.currentTimeMillis()
        )
        // Pakai await() agar proses selesai sebelum lanjut
        db.collection("users").document(uid).set(userMap, SetOptions.merge()).await()
    }
}