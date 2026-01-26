package com.example.luca.data

import android.app.Activity
import com.example.luca.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signUpManual(email: String, pass: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- B. UNTUK LOGIN SCREEN (Manual) ---
    suspend fun loginManual(email: String, pass: String): Result<Boolean> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                // Cek apakah data ada di DB?
                val doc = db.collection("users").document(user.uid).get().await()
                if (doc.exists()) {
                    Result.success(true) // Login Sukses, User Lengkap -> Ke Home
                } else {
                    Result.success(false)
                }
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("user-not-found", true) == true -> "Account does not exist"
                e.message?.contains("password", true) == true -> "Password incorrect"
                else -> e.message ?: "Login failed"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Pair<Boolean, Boolean>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user

            if (user != null) {
                val doc = db.collection("users").document(user.uid).get().await()
                if (doc.exists()) {
                    Result.success(Pair(true, false))
                } else {
                    Result.success(Pair(true, true))
                }
            } else {
                Result.failure(Exception("Google User Null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithTwitter(activity: Activity): Result<Pair<Boolean, Boolean>> {
        return try {
            val provider = OAuthProvider.newBuilder("twitter.com")
            provider.addCustomParameter("lang", "id")
            val result = auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            val user = result.user

            if (user != null) {
                val doc = db.collection("users").document(user.uid).get().await()
                if (doc.exists()) {
                    Result.success(Pair(true, false))
                } else {
                    Result.success(Pair(true, true)) // Ke Fill Profile
                }
            } else {
                Result.failure(Exception("Twitter User Null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(username: String, avatarName: String): Result<Boolean> {
        val user = auth.currentUser ?: return Result.failure(Exception("User offline"))
        return try {
            val userData = User(
                uid = user.uid,
                email = user.email ?: "",
                username = username,
                avatarName = avatarName,
                createdAt = System.currentTimeMillis()
            )
            // Simpan object User ke Firestore
            db.collection("users").document(user.uid)
                .set(userData, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}