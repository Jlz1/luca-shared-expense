package com.example.luca

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

    private fun saveUserToFirestore(uid: String, email: String, name: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to name,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(userMap)
    }
}