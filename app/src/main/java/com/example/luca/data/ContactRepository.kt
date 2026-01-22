package com.example.luca.data

import com.example.luca.model.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Add Contact
    suspend fun addContact(contact: Contact): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))

        return try {
            val newContactRef = db.collection("users").document(uid).collection("contacts").document()
            val finalContact = contact.copy(id = newContactRef.id, userId = uid)
            newContactRef.set(finalContact).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get Contacts
    suspend fun getContacts(): List<Contact> {
        val uid = currentUserId ?: return emptyList()

        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("contacts")
                .get()
                .await()
            snapshot.toObjects(Contact::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Update Contact
    suspend fun updateContact(contactId: String, contact: Contact): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))

        return try {
            val finalContact = contact.copy(id = contactId, userId = uid)
            db.collection("users")
                .document(uid)
                .collection("contacts")
                .document(contactId)
                .set(finalContact)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete Contact
    suspend fun deleteContact(contactId: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))

        return try {
            db.collection("users")
                .document(uid)
                .collection("contacts")
                .document(contactId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}