package com.example.luca.data

import android.net.Uri
import com.example.luca.model.Activity
import com.example.luca.model.Contact
import com.example.luca.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface LucaRepository {
    // Event Actions
    suspend fun createEvent(event: Event): Result<Boolean>
    suspend fun uploadEventImage(imageUri: Uri): String?

    // Data Fetching
    fun getEventsFlow(): Flow<List<Event>>

    // Contact Actions
    fun getContactsFlow(): Flow<List<Contact>>
    suspend fun addContact(contact: Contact): Result<Boolean>

    // --- BARU: Get Current User Profile ---
    suspend fun getCurrentUserAsContact(): Contact?

    // Legacy
    suspend fun getEventById(id: String): Event?
    suspend fun getActivitiesByEventId(eventId: String): List<Activity>
}

class LucaFirebaseRepository : LucaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- IMPLEMENTASI BARU: AMBIL PROFILE SENDIRI ---
    override suspend fun getCurrentUserAsContact(): Contact? {
        val uid = currentUserId ?: return null
        return try {
            // Ambil data dari users/{uid}
            val snapshot = db.collection("users").document(uid).get().await()

            // Ambil username & avatar (Fallback ke default jika null)
            val name = snapshot.getString("username") ?: auth.currentUser?.displayName ?: "Me"
            val avatar = snapshot.getString("avatarName") ?: "avatar_1"

            // Kemas sebagai Contact agar kompatibel dengan list participant
            Contact(
                id = uid,       // ID User sendiri
                userId = uid,   // Owner ID
                name = name,
                avatarName = avatar,
                description = "Host" // Penanda bahwa ini host
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- EVENTS ---
    override suspend fun createEvent(event: Event): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        return try {
            val docRef = if (event.id.isEmpty()) {
                db.collection("users").document(uid).collection("events").document()
            } else {
                db.collection("users").document(uid).collection("events").document(event.id)
            }
            val finalEvent = event.copy(id = docRef.id)
            docRef.set(finalEvent).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEventsFlow(): Flow<List<Event>> = callbackFlow {
        val uid = currentUserId ?: run { close(); return@callbackFlow }
        val subscription = db.collection("users").document(uid).collection("events")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) trySend(snapshot.toObjects(Event::class.java))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun uploadEventImage(imageUri: Uri): String? {
        return try {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("images/events/$fileName.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { null }
    }

    // --- CONTACTS ---
    override fun getContactsFlow(): Flow<List<Contact>> = callbackFlow {
        val uid = currentUserId ?: run { close(); return@callbackFlow }
        val subscription = db.collection("users").document(uid).collection("contacts")
            .orderBy("name")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) trySend(snapshot.toObjects(Contact::class.java))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addContact(contact: Contact): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        return try {
            val ref = db.collection("users").document(uid).collection("contacts").document()
            val finalContact = contact.copy(id = ref.id, userId = uid)
            ref.set(finalContact).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Legacy Stubs
    override suspend fun getEventById(id: String): Event? = null
    override suspend fun getActivitiesByEventId(eventId: String): List<Activity> = emptyList()
}