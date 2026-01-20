package com.example.luca.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun signInWithGoogle(idToken: String): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()

            val user = result.user
            if (user != null) {
                val isNewUser = result.additionalUserInfo?.isNewUser == true
                if (isNewUser) {
                    saveUserToFirestore(user.uid, user.email ?: "", user.displayName ?: "")
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveUserAfterSocialLogin(user: FirebaseUser) {
        saveUserToFirestore(user.uid, user.email ?: "", user.displayName ?: "")
    }

    suspend fun loginManual(email: String, pass: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signUpManual(email: String, pass: String): Boolean {
        return try {
            // Ini perintah inti untuk bikin user baru di Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            // Jika berhasil, kita buat dokumen kosong dulu di Firestore
            if (user != null) {
                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(user.uid).set(userMap, SetOptions.merge()).await()
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

            // Kalau user pilih foto, upload dulu
            if (imageUri != null) {
                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("profile_images/${user.uid}/$fileName")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            // Siapkan data update
            val updates = hashMapOf<String, Any>(
                "username" to username
            )
            // Kalau ada foto, masukkan ke map
            if (imageUrl.isNotEmpty()) {
                updates["profileImageUrl"] = imageUrl
            }

            // Update ke Firestore
            db.collection("users").document(user.uid).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveUserToFirestore(uid: String, email: String, name: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to name,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(userMap, SetOptions.merge())
    }
}