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

    // --- A. UNTUK SIGN UP SCREEN (Return Boolean sesuai UI kamu) ---
    suspend fun signUpManual(email: String, pass: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            // Kita belum simpan ke DB di sini, karena belum ada Username.
            // Data disimpan nanti saat di FillProfileScreen.
            true
        } catch (e: Exception) {
            // Jika email sudah terdaftar, coba sign in untuk cek apakah user hantu
            if (e.message?.contains("already in use", true) == true ||
                e.message?.contains("email-already-in-use", true) == true) {
                try {
                    // Coba sign in dengan kredensial yang sama
                    val result = auth.signInWithEmailAndPassword(email, pass).await()
                    val user = result.user

                    if (user != null) {
                        // Cek apakah data ada di Firestore
                        val doc = db.collection("users").document(user.uid).get().await()
                        if (!doc.exists()) {
                            // User ada di Auth tapi tidak ada di DB (User Hantu)
                            // Biarkan user lanjut ke Fill Profile
                            return true
                        } else {
                            // User sudah lengkap, tidak bisa sign up lagi
                            auth.signOut()
                            return false
                        }
                    } else {
                        return false
                    }
                } catch (signInError: Exception) {
                    // Jika sign in gagal (password salah), tetap return false
                    signInError.printStackTrace()
                    return false
                }
            }

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
                    Result.success(true) // Login Sukses, User Valid
                } else {
                    // Auth berhasil, tapi DB tidak ada.
                    // Untuk Login Manual, kita anggap ini error "Account does not exist"
                    // supaya user daftar ulang atau tidak bingung.
                    auth.signOut()
                    Result.failure(Exception("Account does not exist"))
                }
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            // Mapping Error Firebase ke pesan yang dimengerti UI
            val msg = when {
                e.message?.contains("user-not-found", true) == true -> "Account does not exist"
                e.message?.contains("password", true) == true -> "Password incorrect"
                else -> e.message ?: "Login failed"
            }
            Result.failure(Exception(msg))
        }
    }

    // --- C. UNTUK GOOGLE & TWITTER (Cek User Hantu) ---
    // Return: Pair<Boolean, Boolean> -> (IsSuccess, IsNewUser/GhostUser)
    suspend fun signInWithGoogle(idToken: String): Result<Pair<Boolean, Boolean>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user

            if (user != null) {
                val doc = db.collection("users").document(user.uid).get().await()
                if (doc.exists()) {
                    // User Lama & Data Lengkap -> Ke Home
                    Result.success(Pair(true, false))
                } else {
                    // User Baru ATAU User Hantu (Data DB terhapus) -> Ke Fill Profile
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

    // --- D. UNTUK FILL PROFILE SCREEN (Simpan ke DB) ---
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
                .set(userData, SetOptions.merge()) // Merge biar aman
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}