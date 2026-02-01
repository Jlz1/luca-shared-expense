package com.example.luca.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
import java.util.UUID

interface LucaRepository {
    // Event Actions
    suspend fun createEvent(event: Event): Result<Boolean>
    suspend fun uploadEventImage(imageUri: Uri): String?

    // Activity Actions
    suspend fun createActivity(eventId: String, activity: Activity): Result<String>
    suspend fun getActivitiesByEventId(eventId: String): List<Activity>
    suspend fun getActivityById(eventId: String, activityId: String): Activity?
    suspend fun getParticipantsInActivities(eventId: String): List<String>
    suspend fun saveActivityItems(eventId: String, activityId: String, items: List<Any>, taxPercentage: Double, discountAmount: Double): Result<Boolean>
    suspend fun getActivityItems(eventId: String, activityId: String): List<Map<String, Any>>

    // Data Fetching
    fun getEventsFlow(): Flow<List<Event>>

    // Contact Actions
    fun getContactsFlow(): Flow<List<Contact>>
    suspend fun addContact(contact: Contact): Result<Boolean>

    // Get Current User Profile
    suspend fun getCurrentUserAsContact(): Contact?

    // Legacy / Details
    suspend fun getEventById(id: String): Event?

    suspend fun deleteEvent(eventId: String): Result<Boolean>
}

// Konstruktor menerima Context untuk keperluan kompresi gambar
class LucaFirebaseRepository(private val context: Context? = null) : LucaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- IMPLEMENTASI ---

    override suspend fun getCurrentUserAsContact(): Contact? {
        val uid = currentUserId ?: return null
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            val name = snapshot.getString("username") ?: auth.currentUser?.displayName ?: "Me"
            val avatar = snapshot.getString("avatarName") ?: "avatar_1"

            Contact(
                id = uid,
                userId = uid,
                name = name,
                avatarName = avatar,
                description = "Host"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createEvent(event: Event): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        return try {
            // Jika ID kosong, buat dokumen baru. Jika tidak, pakai ID lama (Update).
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

            // Jika Context ada, lakukan kompresi
            if (context != null) {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                val baos = ByteArrayOutputStream()
                // Kompres ke JPEG kualitas 50%
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data = baos.toByteArray()

                ref.putBytes(data).await()
            } else {
                // Fallback jika context null (Upload file mentah)
                ref.putFile(imageUri).await()
            }

            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    override suspend fun getEventById(id: String): Event? {
        val uid = currentUserId ?: return null
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
        val uid = currentUserId ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .get()
                .await()
            snapshot.toObjects(Activity::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getActivityById(eventId: String, activityId: String): Activity? {
        val uid = currentUserId ?: return null
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document(activityId)
                .get()
                .await()
            snapshot.toObject(Activity::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createActivity(eventId: String, activity: Activity): Result<String> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        return try {
            val docRef = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document()
            val finalActivity = activity.copy(id = docRef.id, eventId = eventId)
            docRef.set(finalActivity).await()
            android.util.Log.d("LucaRepository", "Activity created with ID: ${docRef.id}")
            Result.success(docRef.id) // Return activity ID
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(eventId: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        return try {
            db.collection("users").document(uid).collection("events").document(eventId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getParticipantsInActivities(eventId: String): List<String> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val activities = getActivitiesByEventId(eventId)
            // Collect all participant names from all activities
            activities.flatMap { activity ->
                activity.participants.map { it.name }
            }.distinct()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun saveActivityItems(
        eventId: String,
        activityId: String,
        items: List<Any>,
        taxPercentage: Double,
        discountAmount: Double
    ): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            android.util.Log.d("LucaRepository", "=== START saveActivityItems ===")
            android.util.Log.d("LucaRepository", "UserID: $uid")
            android.util.Log.d("LucaRepository", "EventID: $eventId")
            android.util.Log.d("LucaRepository", "ActivityID: $activityId")
            android.util.Log.d("LucaRepository", "Items count: ${items.size}")
            
            if (eventId.isEmpty() || activityId.isEmpty()) {
                android.util.Log.e("LucaRepository", "ERROR: EventID or ActivityID is empty!")
                return Result.failure(Exception("EventID or ActivityID is empty"))
            }
            
            // Reference to items collection
            val itemsCollectionRef = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document(activityId)
                .collection("items")

            // First, delete all existing items
            val existingSnapshot = itemsCollectionRef.get().await()
            for (doc in existingSnapshot.documents) {
                doc.reference.delete().await()
                android.util.Log.d("LucaRepository", "Deleted existing item: ${doc.id}")
            }

            // Then save new items
            for (item in items) {
                @Suppress("UNCHECKED_CAST")
                val itemMap = item as? Map<String, Any> ?: continue

                // Extract and convert fields with proper type handling
                val itemName = itemMap["itemName"]?.toString() ?: ""
                val price = when (itemMap["price"]) {
                    is Long -> itemMap["price"] as Long
                    is Int -> (itemMap["price"] as Int).toLong()
                    is String -> (itemMap["price"] as String).toLongOrNull() ?: 0L
                    else -> 0L
                }
                val quantity = when (itemMap["quantity"]) {
                    is Int -> itemMap["quantity"] as Int
                    is Long -> (itemMap["quantity"] as Long).toInt()
                    is String -> (itemMap["quantity"] as String).toIntOrNull() ?: 1
                    else -> 1
                }
                @Suppress("UNCHECKED_CAST")
                val memberNames = (itemMap["memberNames"] as? List<String>) ?: emptyList()
                val timestamp = itemMap["timestamp"] as? Long ?: System.currentTimeMillis()

                val itemData = mapOf(
                    "itemName" to itemName,
                    "price" to price,
                    "quantity" to quantity,
                    "memberNames" to memberNames,
                    "taxPercentage" to taxPercentage,
                    "discountAmount" to discountAmount,
                    "timestamp" to timestamp
                )

                // Save each item
                val docRef = itemsCollectionRef.document()
                docRef.set(itemData).await()
                android.util.Log.d("LucaRepository", "✅ Saved item [${docRef.id}]: $itemName (Qty: $quantity, Price: $price)")
            }

            android.util.Log.d("LucaRepository", "✅ === END saveActivityItems SUCCESS ===")
            Result.success(true)
            
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "❌ ERROR in saveActivityItems: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getActivityItems(eventId: String, activityId: String): List<Map<String, Any>> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val itemsSnap = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document(activityId)
                .collection("items")
                .get()
                .await()
            itemsSnap.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}