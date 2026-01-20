package com.example.luca.data

import android.net.Uri
import android.util.Log
import com.example.luca.model.Activity
import com.example.luca.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface LucaRepository {
    // UPDATED: Semua butuh userId untuk masuk ke "kamar" yang tepat
    suspend fun getAllEvents(userId: String): List<Event>
    suspend fun getEventById(userId: String, eventId: String): Event?

    // Create juga butuh userId
    suspend fun createEvent(userId: String, event: Event): Boolean

    // Upload image tidak butuh userId (karena masuk storage umum), tapi boleh ditambah kalau mau rapi
    suspend fun uploadEventImage(imageUri: Uri): String?

    // Activities sementara kita biarkan dulu (asumsi masih di root atau logic lain)
    suspend fun getActivitiesByEventId(eventId: String): List<Activity>
}

class LucaFirebaseRepository : LucaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // CATATAN: eventsCollection dihapus dari sini karena sekarang jalurnya dinamis tergantung userId
    private val activitiesCollection = db.collection("activities") // Ini biarkan dulu

    // --- FUNGSI CREATE (UPDATED) ---

    override suspend fun createEvent(userId: String, event: Event): Boolean {
        return try {
            // JALUR BARU: users -> userId -> events -> eventId
            db.collection("users")
                .document(userId)
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

    // --- FUNGSI GET DATA (UPDATED) ---

    override suspend fun getAllEvents(userId: String): List<Event> {
        return try {
            Log.d("FIREBASE_DEBUG", "Masuk ke collection events milik user: $userId")

            // JALUR BARU: Ambil dari sub-collection
            val snapshot = db.collection("users")
                .document(userId)
                .collection("events")
                .get()
                .await()

            Log.d("FIREBASE_DEBUG", "Ditemukan: ${snapshot.size()} event")

            val events = snapshot.toObjects<Event>()
            events
        } catch (e: Exception) {
            Log.e("FIREBASE_ERROR", "Gagal ambil data: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEventById(userId: String, eventId: String): Event? {
        return try {
            // JALUR BARU: Langsung tembak ke dokumen spesifik di dalam sub-collection
            val snapshot = db.collection("users")
                .document(userId)
                .collection("events")
                .document(eventId)
                .get()
                .await()

            snapshot.toObject(Event::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getActivitiesByEventId(eventId: String): List<Activity> {
        return try {
            // Sementara activities masih ambil dari root collection (sesuai kode lama kamu)
            // Kalau activities mau dimasukin ke user juga, nanti kita update lagi
            val snapshot = activitiesCollection.whereEqualTo("eventId", eventId).get().await()
            snapshot.toObjects<Activity>()
        } catch (e: Exception) { emptyList() }
    }
}