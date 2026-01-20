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

    // --- 1. GOOGLE LOGIN ---
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
                Result.success("Login Google Berhasil")
            } else {
                Result.failure(Exception("Gagal login: User null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2. X (TWITTER) LOGIN ---
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

    // --- 3. REGISTER MANUAL (Versi Result - Future Proof) ---
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

    // --- 5. FUNGSI LEGACY (Dipakai SignUpScreen saat ini) ---
    suspend fun signUpManual(email: String, pass: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                // Buat dokumen user kosong (atau dengan email)
                saveUserToFirestore(user.uid, email, "")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateProfile(username: String, imageUri: Uri?): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            var imageUrl = ""

            if (imageUri != null) {
                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("profile_images/${user.uid}/$fileName")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            val updates = hashMapOf<String, Any>(
                "username" to username
            )
            if (imageUrl.isNotEmpty()) {
                updates["profileImageUrl"] = imageUrl
            }

            db.collection("users").document(user.uid).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- HELPER ---
    fun saveUserAfterSocialLogin(user: FirebaseUser) {
        // Placeholder untuk kompatibilitas
    }

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