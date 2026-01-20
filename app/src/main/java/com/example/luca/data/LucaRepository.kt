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
    private val auth = FirebaseAuth.getInstance() // ✅ Tambahan: Buat ambil UID

    // ❌ eventsCollection yang lama DIHAPUS karena path-nya sekarang berubah-ubah tergantung user.
    // private val eventsCollection = db.collection("events")

    // ✅ activities tetap global dulu (kecuali kamu mau ubah logic Add Activity-nya juga nanti)
    private val activitiesCollection = db.collection("activities")

    // --- HELPER FUNCTION: Ambil UID User Sekarang ---
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // --- FUNGSI CREATE EVENT (VERSI BARU - SUB-COLLECTION) ---
    override suspend fun createEvent(event: Event): Boolean {
        val uid = getCurrentUserId()
        if (uid == null) {
            Log.e("FIREBASE_ERROR", "Gagal Create: User belum login!")
            return false
        }

        return try {
            // ✅ PATH BARU: users/{uid}/events/{eventId}
            // Masuk ke laci pribadi user, bukan laci umum.
            db.collection("users")
                .document(uid)
                .collection("events")
                .document(event.id)
                .set(event)
                .await()

            Log.d("FIREBASE_SUCCESS", "Event berhasil disimpan di folder user: $uid")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FIREBASE_ERROR", "Error writing document", e)
            false
        }
    }

    override suspend fun uploadEventImage(imageUri: Uri): String? {
        return try {
            val fileName = UUID.randomUUID().toString()
            // Opsional: Bisa dirapikan jadi images/users/{uid}/{fileName} kalau mau
            val ref = storage.reference.child("images/events/$fileName.jpg")

            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- FUNGSI GET DATA (VERSI BARU - SUB-COLLECTION) ---

    override suspend fun getAllEvents(): List<Event> {
        val uid = getCurrentUserId()
        if (uid == null) return emptyList()

        return try {
            // ✅ PATH BARU: Ambil cuma dari laci user ini
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .get()
                .await()

            Log.d("FIREBASE_DEBUG", "Jumlah dokumen user $uid: ${snapshot.size()}")
            val events = snapshot.toObjects<Event>()
            events
        } catch (e: Exception) {
            Log.e("FIREBASE_ERROR", "Gagal ambil data: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEventById(id: String): Event? {
        val uid = getCurrentUserId() ?: return null

        return try {
            // ✅ OPTIMASI: Langsung tembak ID-nya (Direct Lookup), gak perlu searching (Query)
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .document(id) // Langsung ke dokumen spesifik
                .get()
                .await()

            snapshot.toObject(Event::class.java)
        } catch (e: Exception) {
            Log.e("FIREBASE_ERROR", "Gagal get detail event: ${e.message}")
            null
        }
    }

    override suspend fun getActivitiesByEventId(eventId: String): List<Activity> {
        // Catatan: Ini masih mengambil dari koleksi global "activities".
        // Kalau kamu nanti mau activities-nya private juga, logic AddActivity harus diubah dulu.
        // Untuk sekarang, biarkan begini biar tidak error.
        return try {
            val snapshot = activitiesCollection.whereEqualTo("eventId", eventId).get().await()
            snapshot.toObjects<Activity>()
        } catch (e: Exception) { emptyList() }
    }
}