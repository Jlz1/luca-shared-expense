package com.example.luca.data

import android.net.Uri
import com.example.luca.model.Activity
import com.example.luca.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface LucaRepository {
    suspend fun getAllEvents(): List<Event>
    suspend fun getEventById(id: String): Event?
    suspend fun getActivitiesByEventId(eventId: String): List<Activity>

    // FUNGSI BARU: Upload & Create
    suspend fun createEvent(event: Event): Boolean
    suspend fun uploadEventImage(imageUri: Uri): String?
}

class LucaFirebaseRepository : LucaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val eventsCollection = db.collection("events")
    private val activitiesCollection = db.collection("activities")

    // --- FUNGSI BARU UNTUK ADD EVENT ---

    override suspend fun createEvent(event: Event): Boolean {
        return try {
            // Simpan event dengan ID yang sudah digenerate
            eventsCollection.document(event.id).set(event).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun uploadEventImage(imageUri: Uri): String? {
        return try {
            // Generate nama file unik biar gak bentrok
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("images/events/$fileName.jpg")

            // Upload file
            ref.putFile(imageUri).await()

            // Ambil URL download-nya untuk disimpan di database
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- FUNGSI LAMA (GET DATA) ---

    override suspend fun getAllEvents(): List<Event> {
        return try {
            val snapshot = eventsCollection.get().await()
            snapshot.toObjects<Event>()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getEventById(id: String): Event? {
        return try {
            val snapshot = eventsCollection.whereEqualTo("id", id).get().await()
            if (!snapshot.isEmpty) snapshot.documents[0].toObject(Event::class.java) else null
        } catch (e: Exception) { null }
    }

    override suspend fun getActivitiesByEventId(eventId: String): List<Activity> {
        return try {
            val snapshot = activitiesCollection.whereEqualTo("eventId", eventId).get().await()
            snapshot.toObjects<Activity>()
        } catch (e: Exception) { emptyList() }
    }
}