package com.example.luca.data

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- REGISTER MANUAL ---
    suspend fun signUpManual(email: String, pass: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                // Simpan data dasar, default avatar_1
                saveUserToFirestore(user.uid, email)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- UPDATE PROFILE (FIX: Terima String avatarName) ---
    suspend fun updateProfile(username: String, avatarName: String): Result<Boolean> {
        val user = auth.currentUser ?: return Result.failure(Exception("User offline"))
        return try {
            // Simpan nama avatar (misal: "avatar_5") ke Firestore
            val updates = hashMapOf<String, Any>(
                "username" to username,
                "avatarName" to avatarName
            )

            db.collection("users").document(user.uid)
                .set(updates, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // --- HELPER DATABASE ---
    private suspend fun saveUserToFirestore(uid: String, email: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to "",
            "avatarName" to "avatar_1", // Default awal
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(userMap, SetOptions.merge()).await()
    }

    // --- SOCIAL LOGIN (Google) ---
    suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                if (result.additionalUserInfo?.isNewUser == true) {
                    saveUserToFirestore(user.uid, user.email ?: "")
                }
                Result.success("Login Google Berhasil")
            } else {
                Result.failure(Exception("User null"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- CHECK IF USER EXISTS IN FIRESTORE ---
    suspend fun checkUserExists(uid: String): Boolean {
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- MANUAL LOGIN ---
    suspend fun loginManual(email: String, pass: String): Result<String> {
        return try {
            // Step 1: Authenticate with Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                // Step 2: Check if user exists in Firestore database
                val userExists = checkUserExists(user.uid)

                if (!userExists) {
                    // User authenticated but not in database - sign them out
                    auth.signOut()
                    return Result.failure(Exception("Account does not exist. Please sign up first."))
                }

                // User exists in database, login successful
                Result.success("Login Berhasil")
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            // Handle authentication errors
            val errorMessage = when {
                e.message?.contains("password") == true -> "Wrong email or password"
                e.message?.contains("user") == true -> "Account does not exist. Please sign up first."
                e.message?.contains("network") == true -> "Network error. Please check your connection."
                else -> e.message ?: "Login failed"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    // --- Code Legacy/Tidak Terpakai ---
    fun saveUserAfterSocialLogin(user: FirebaseUser) {}

    suspend fun signInWithTwitter(activity: Activity): Result<String> {
        return Result.failure(Exception("Not implemented"))
    }

    suspend fun registerManual(email: String, pass: String, name: String): Result<String> {
        return Result.failure(Exception("Use signUpManual instead"))
    }
}