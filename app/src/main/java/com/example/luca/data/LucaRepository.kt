package com.example.luca.data

import android.net.Uri
import android.util.Log
import com.example.luca.model.Activity
import com.example.luca.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface LucaRepository {
    // UPDATED: Tidak perlu passing userId dari luar, repo cari sendiri
    suspend fun getAllEvents(): List<Event>
    suspend fun getEventById(id: String): Event?

    // Create event otomatis masuk ke user yang sedang login
    suspend fun createEvent(event: Event): Boolean

    suspend fun uploadEventImage(imageUri: Uri): String?
    suspend fun getActivitiesByEventId(eventId: String): List<Activity>
}

class LucaFirebaseRepository : LucaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Activities sementara masih global (belum per user)
    private val activitiesCollection = db.collection("activities")

    // --- HELPER: Ambil ID User Sendiri ---
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // --- CREATE ---
    override suspend fun createEvent(event: Event): Boolean {
        val uid = getCurrentUserId()
        if (uid == null) {
            Log.e("FIREBASE", "User belum login, gagal create event")
            return false
        }

        return try {
            // Simpan di: users/{uid}/events/{eventId}
            db.collection("users")
                .document(uid)
                .collection("events")
                .document(event.id)
                .set(event)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun uploadEventImage(imageUri: Uri): String? {
        return try {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("images/events/$fileName.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- GET DATA ---
    override suspend fun getAllEvents(): List<Event> {
        val uid = getCurrentUserId() ?: return emptyList()

        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .get()
                .await()

            snapshot.toObjects<Event>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEventById(id: String): Event? {
        val uid = getCurrentUserId() ?: return null

        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .document(id)
                .get()
                .await()

            snapshot.toObject(Event::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getActivitiesByEventId(eventId: String): List<Activity> {
        return try {
            // Sementara ambil dari collection global "activities"
            val snapshot = activitiesCollection.whereEqualTo("eventId", eventId).get().await()
            snapshot.toObjects<Activity>()
        } catch (e: Exception) { emptyList() }
    }
}