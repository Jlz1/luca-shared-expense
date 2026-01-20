package com.example.luca.data

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --- REGISTER MANUAL (Hanya Email & Pass) ---
    suspend fun signUpManual(email: String, pass: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                // Simpan data dasar saja, username dikosongkan dulu
                saveUserToFirestore(user.uid, email)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- UPDATE PROFILE (Isi Username & Foto) ---
    suspend fun updateProfile(username: String, imageUri: Uri?): Result<Boolean> {
        val user = auth.currentUser ?: return Result.failure(Exception("User offline"))
        return try {
            var imageUrl = ""

            if (imageUri != null) {
                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("profile_images/${user.uid}/$fileName")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            // Update Username di sini
            val updates = hashMapOf<String, Any>(
                "username" to username
            )
            if (imageUrl.isNotEmpty()) {
                updates["profileImageUrl"] = imageUrl
            }

            db.collection("users").document(user.uid)
                .set(updates, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // --- DATABASE HELPER ---
    private suspend fun saveUserToFirestore(uid: String, email: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to "", // Username kosong dulu, nanti diisi di FillProfile
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(userMap, SetOptions.merge()).await()
    }

    // --- SOCIAL LOGIN (Tetap Sama) ---
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

    // Fungsi registerManual (cadangan) juga disesuaikan kalau mau dipakai
    suspend fun registerManual(email: String, pass: String, name: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                // Di sini 'name' bisa dimasukkan ke username atau field lain jika perlu
                saveUserToFirestore(user.uid, email)
            }
            Result.success("Registrasi Berhasil")
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun loginManual(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Login Berhasil")
        } catch (e: Exception) { Result.failure(e) }
    }

    fun saveUserAfterSocialLogin(user: FirebaseUser) {}

    suspend fun signInWithTwitter(activity: Activity): Result<String> {
        // ... (Biarkan kode twitter lama atau hapus jika tidak dipakai)
        return Result.failure(Exception("Not implemented"))
    }
}