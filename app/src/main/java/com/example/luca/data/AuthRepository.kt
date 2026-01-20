package com.example.luca.data

import android.app.Activity
import com.example.luca.model.User // Pastikan ini sesuai package model User kamu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- 1. GOOGLE LOGIN (Mengembalikan Result, bukan Boolean) ---
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()

            val user = result.user
            if (user != null) {
                val isNewUser = result.additionalUserInfo?.isNewUser == true
                if (isNewUser) {
                    saveUserToFirestore(user.uid, user.email ?: "", user.displayName ?: "")
                }
                // SUKSES: Kirim Result.success
                Result.success("Login Google Berhasil")
            } else {
                // GAGAL: Kirim Result.failure
                Result.failure(Exception("Gagal login: User null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2. X (TWITTER) LOGIN (Mengembalikan Result) ---
    suspend fun signInWithTwitter(activity: Activity): Result<String> {
        return try {
            val provider = OAuthProvider.newBuilder("twitter.com")
            provider.addCustomParameter("lang", "id")

            val result = auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            val user = result.user

            if (user != null) {
                val isNewUser = result.additionalUserInfo?.isNewUser == true
                if (isNewUser) {
                    val username = result.additionalUserInfo?.profile?.get("screen_name") as? String
                        ?: user.displayName
                        ?: "Twitter User"
                    saveUserToFirestore(user.uid, user.email ?: "", username)
                }
                Result.success("Login Twitter Berhasil")
            } else {
                Result.failure(Exception("Gagal login Twitter"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 3. REGISTER MANUAL ---
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

    // --- 4. LOGIN MANUAL ---
    suspend fun loginManual(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Login Berhasil")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- HELPER UNTUK SOCIAL LOGIN (OPSIONAL JIKA MASIH DIPAKAI KODE LAMA) ---
    // Fungsi ini aman dibiarkan ada kalau-kalau ada kode lama yg manggil
    fun saveUserAfterSocialLogin(user: FirebaseUser) {
        // Kita biarkan kosong atau panggil saveUserToFirestore secara background
        // karena logic utama sudah ada di signInWithTwitter/Google di atas
    }

    // --- DATABASE HELPER ---
    private suspend fun saveUserToFirestore(uid: String, email: String, name: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to name,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(userMap, SetOptions.merge()).await()
    }
}