package com.luca.shared.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.luca.shared.model.Activity
import com.luca.shared.model.Contact
import com.luca.shared.model.Event
import com.luca.shared.model.NotificationPreferences
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
    suspend fun getActivityData(eventId: String, activityId: String): Map<String, Any>?
    suspend fun getParticipantsInActivities(eventId: String): List<String>
    suspend fun saveActivityItems(eventId: String, activityId: String, items: List<Any>, taxPercentage: Double, globalTax: Double = 0.0, serviceCharge: Double = 0.0, discountAmount: Double, isSplitEqual: Boolean = false): Result<Boolean>
    suspend fun getActivityItems(eventId: String, activityId: String): List<Map<String, Any>>
    suspend fun deleteActivity(eventId: String, activityId: String): Result<Boolean>

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

    // Settlement Actions
    suspend fun saveSettlementResult(eventId: String, settlementJson: String): Result<Boolean>
    suspend fun getSettlementResult(eventId: String): String?

    // Notification Preferences
    suspend fun saveNotificationPreferences(userId: String, preferences: NotificationPreferences): Result<Boolean>
    suspend fun getNotificationPreferences(userId: String): NotificationPreferences?
}

// Konstruktor menerima Context untuk keperluan kompresi gambar
class LucaFirebaseRepository(private val context: Context? = null) : LucaRepository {

    private val db = FirebaseFirestore.getInstance()
    // Point to the specific Firebase Storage bucket provided by the user
    private val storage = FirebaseStorage.getInstance("gs://luca-f40d7.firebasestorage.app")
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
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) trySend(snapshot.toObjects(Event::class.java))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun uploadEventImage(imageUri: Uri): String? {
        return try {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("images/events/$fileName.jpg")

            android.util.Log.d("LucaRepository", "uploadEventImage: start uri=$imageUri to ${ref.path}")

            // Jika Context ada, lakukan kompresi kualitas-preserving
            if (context != null) {
                val inputStream = try { context.contentResolver.openInputStream(imageUri) } catch (e: Exception) { null }
                if (inputStream == null) {
                    android.util.Log.e("LucaRepository", "uploadEventImage: inputStream null for uri=$imageUri")
                    // Fallback ke putFile agar tetap ada peluang upload
                    ref.putFile(imageUri).await()
                } else {
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    try { inputStream.close() } catch (_: Exception) {}

                    if (originalBitmap == null) {
                        android.util.Log.e("LucaRepository", "uploadEventImage: decoded bitmap null, fallback putFile")
                        ref.putFile(imageUri).await()
                    } else {
                        // Resize terlebih dahulu agar dimensi tidak terlalu besar (maks 1920px pada sisi terpanjang)
                        val maxSize = 1920
                        val longestSide = maxOf(originalBitmap.width, originalBitmap.height)
                        val ratio = maxSize.toFloat() / longestSide.toFloat()
                        val resizedBitmap = if (ratio < 1f) {
                            Bitmap.createScaledBitmap(
                                originalBitmap,
                                (originalBitmap.width * ratio).toInt(),
                                (originalBitmap.height * ratio).toInt(),
                                true
                            )
                        } else {
                            originalBitmap
                        }

                        // Iterative compress hingga ukuran <= 1MB, menjaga kualitas setinggi mungkin
                        val targetBytes = 1_000_000 // ~1MB
                        var quality = 85
                        var data: ByteArray
                        var loops = 0
                        do {
                            val baos = ByteArrayOutputStream()
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                            data = baos.toByteArray()
                            android.util.Log.d("LucaRepository", "uploadEventImage: try#$loops quality=$quality size=${data.size}")
                            quality -= 5
                            loops++
                        } while (data.size > targetBytes && quality >= 50)

                        android.util.Log.d("LucaRepository", "uploadEventImage: final size=${data.size} bytes, quality=${quality + 5}")
                        ref.putBytes(data).await()
                      }
                }
            } else {
                // Fallback jika context null (Upload file mentah)
                android.util.Log.w("LucaRepository", "uploadEventImage: context null, using putFile")
                ref.putFile(imageUri).await()
            }

            val url = ref.downloadUrl.await().toString()
            android.util.Log.d("LucaRepository", "uploadEventImage: success url=$url")
            url
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "uploadEventImage: error ${e.message}")
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
                .orderBy("createdAt", Query.Direction.DESCENDING)
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

    override suspend fun getActivityData(eventId: String, activityId: String): Map<String, Any>? {
        val uid = currentUserId ?: return null
        return try {
            android.util.Log.d("LucaRepository", "=== Getting Activity Data ===")
            android.util.Log.d("LucaRepository", "EventID: $eventId, ActivityID: $activityId")
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document(activityId)
                .get()
                .await()
            val data = snapshot.data
            android.util.Log.d("LucaRepository", "Activity data retrieved: $data")
            if (data != null) {
                android.util.Log.d("LucaRepository", "Tax: ${data["taxPercentage"]}, Discount: ${data["discountAmount"]}")
            } else {
                android.util.Log.w("LucaRepository", "⚠️ Activity document data is NULL!")
            }
            data
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "❌ Error getting activity data: ${e.message}")
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
        globalTax: Double,
        serviceCharge: Double,
        discountAmount: Double,
        isSplitEqual: Boolean
    ): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            android.util.Log.d("LucaRepository", "=== START saveActivityItems ===")
            android.util.Log.d("LucaRepository", "UserID: $uid")
            android.util.Log.d("LucaRepository", "EventID: $eventId")
            android.util.Log.d("LucaRepository", "ActivityID: $activityId")
            android.util.Log.d("LucaRepository", "Items count: ${items.size}")
            android.util.Log.d("LucaRepository", "Tax Percentage: $taxPercentage%")
            android.util.Log.d("LucaRepository", "Global Tax: $globalTax")
            android.util.Log.d("LucaRepository", "Service Charge: $serviceCharge")
            android.util.Log.d("LucaRepository", "Discount Amount: $discountAmount")
            android.util.Log.d("LucaRepository", "Equal Split: $isSplitEqual")

            if (eventId.isEmpty() || activityId.isEmpty()) {
                android.util.Log.e("LucaRepository", "ERROR: EventID or ActivityID is empty!")
                return Result.failure(Exception("EventID or ActivityID is empty"))
            }
            
            // Reference to Activity document
            val activityDocRef = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document(activityId)

            // PENTING: Simpan tax%, globalTax, service charge, discount, dan isSplitEqual di level Activity document
            // Gunakan set dengan merge agar field lain tidak terhapus
            activityDocRef.set(
                mapOf(
                    "taxPercentage" to taxPercentage,
                    "globalTax" to globalTax,
                    "serviceCharge" to serviceCharge,
                    "discountAmount" to discountAmount,
                    "isSplitEqual" to isSplitEqual
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            android.util.Log.d("LucaRepository", "✅ Saved tax%, globalTax, service charge, discount, and isSplitEqual to Activity document (tax%: $taxPercentage%, globalTax: $globalTax, service: $serviceCharge, discount: $discountAmount, isSplitEqual: $isSplitEqual)")

            // Reference to items collection
            val itemsCollectionRef = activityDocRef.collection("items")

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

                // Extract item-specific tax and discount
                val itemTax = when (itemMap["itemTax"]) {
                    is Double -> itemMap["itemTax"] as Double
                    is Int -> (itemMap["itemTax"] as Int).toDouble()
                    is Long -> (itemMap["itemTax"] as Long).toDouble()
                    is String -> (itemMap["itemTax"] as String).toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val itemDiscount = when (itemMap["itemDiscount"]) {
                    is Double -> itemMap["itemDiscount"] as Double
                    is Int -> (itemMap["itemDiscount"] as Int).toDouble()
                    is Long -> (itemMap["itemDiscount"] as Long).toDouble()
                    is String -> (itemMap["itemDiscount"] as String).toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                val itemData = mapOf(
                    "itemName" to itemName,
                    "price" to price,
                    "quantity" to quantity,
                    "memberNames" to memberNames,
                    "taxPercentage" to taxPercentage,
                    "discountAmount" to discountAmount,
                    "itemTax" to itemTax,
                    "itemDiscount" to itemDiscount,
                    "timestamp" to timestamp
                )

                // Save each item
                val docRef = itemsCollectionRef.document()
                docRef.set(itemData).await()
                android.util.Log.d("LucaRepository", "✅ Saved item [${docRef.id}]: $itemName (Qty: $quantity, Price: $price, Tax: $taxPercentage%, Discount: $discountAmount, ItemTax: $itemTax, ItemDiscount: $itemDiscount)")
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
            android.util.Log.d("LucaRepository", "=== Loading items from Firestore ===")
            android.util.Log.d("LucaRepository", "EventID: $eventId, ActivityID: $activityId")

            val itemsSnap = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document(activityId)
                .collection("items")
                .get()
                .await()

            val items = itemsSnap.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                android.util.Log.d("LucaRepository", "📦 Item loaded: ${data["itemName"]}, Tax: ${data["taxPercentage"]}, Discount: ${data["discountAmount"]}")
                data
            }

            android.util.Log.d("LucaRepository", "✅ Total items loaded: ${items.size}")
            items
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "❌ Error loading items: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun deleteActivity(eventId: String, activityId: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        return try {
            android.util.Log.d("LucaRepository", "Deleting activity: $activityId from event: $eventId")

            // Delete the activity document and all its subcollections (items)
            val activityRef = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .collection("activities")
                .document(activityId)

            // First delete all items in the activity
            val itemsSnap = activityRef.collection("items").get().await()
            itemsSnap.documents.forEach { it.reference.delete().await() }

            // Then delete the activity itself
            activityRef.delete().await()

            android.util.Log.d("LucaRepository", "✅ Activity deleted successfully")
            Result.success(true)
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "❌ Error deleting activity: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Menyimpan hasil settlement calculation sebagai JSON string di Event document.
     * Field yang digunakan: settlementResultJson
     */
    override suspend fun saveSettlementResult(eventId: String, settlementJson: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        return try {
            android.util.Log.d("LucaRepository", "Saving settlement result for event: $eventId")

            db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .update("settlementResultJson", settlementJson)
                .await()

            android.util.Log.d("LucaRepository", "✅ Settlement result saved successfully")
            Result.success(true)
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "❌ Error saving settlement result: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Mengambil hasil settlement calculation dari Event document.
     * @return JSON string of SettlementResult or null if not exists
     */
    override suspend fun getSettlementResult(eventId: String): String? {
        val uid = currentUserId ?: return null
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("events")
                .document(eventId)
                .get()
                .await()

            snapshot.getString("settlementResultJson")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Menyimpan preferensi notifikasi user ke Firestore
     */
    override suspend fun saveNotificationPreferences(
        userId: String,
        preferences: NotificationPreferences
    ): Result<Boolean> {
        return try {
            val preferencesMap = mapOf(
                "pushEnabled" to preferences.pushEnabled,
                "pushNewExpense" to preferences.pushNewExpense,
                "pushPaymentReminder" to preferences.pushPaymentReminder,
                "pushGroupInvite" to preferences.pushGroupInvite,
                "pushExpenseUpdate" to preferences.pushExpenseUpdate,
                "emailEnabled" to preferences.emailEnabled,
                "emailWeeklySummary" to preferences.emailWeeklySummary,
                "emailPaymentReminder" to preferences.emailPaymentReminder,
                "emailGroupActivity" to preferences.emailGroupActivity,
                "doNotDisturbEnabled" to preferences.doNotDisturbEnabled,
                "doNotDisturbStart" to preferences.doNotDisturbStart,
                "doNotDisturbEnd" to preferences.doNotDisturbEnd
            )

            db.collection("users")
                .document(userId)
                .collection("settings")
                .document("notifications")
                .set(preferencesMap)
                .await()

            android.util.Log.d("LucaRepository", "✅ Notification preferences saved successfully")
            Result.success(true)
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "❌ Error saving notification preferences: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Mengambil preferensi notifikasi user dari Firestore
     */
    override suspend fun getNotificationPreferences(userId: String): NotificationPreferences? {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("settings")
                .document("notifications")
                .get()
                .await()

            if (snapshot.exists()) {
                NotificationPreferences(
                    pushEnabled = snapshot.getBoolean("pushEnabled") ?: true,
                    pushNewExpense = snapshot.getBoolean("pushNewExpense") ?: true,
                    pushPaymentReminder = snapshot.getBoolean("pushPaymentReminder") ?: true,
                    pushGroupInvite = snapshot.getBoolean("pushGroupInvite") ?: true,
                    pushExpenseUpdate = snapshot.getBoolean("pushExpenseUpdate") ?: true,
                    emailEnabled = snapshot.getBoolean("emailEnabled") ?: true,
                    emailWeeklySummary = snapshot.getBoolean("emailWeeklySummary") ?: true,
                    emailPaymentReminder = snapshot.getBoolean("emailPaymentReminder") ?: true,
                    emailGroupActivity = snapshot.getBoolean("emailGroupActivity") ?: false,
                    doNotDisturbEnabled = snapshot.getBoolean("doNotDisturbEnabled") ?: false,
                    doNotDisturbStart = snapshot.getString("doNotDisturbStart") ?: "22:00",
                    doNotDisturbEnd = snapshot.getString("doNotDisturbEnd") ?: "07:00"
                )
            } else {
                // Return default preferences if not exists
                NotificationPreferences()
            }
        } catch (e: Exception) {
            android.util.Log.e("LucaRepository", "❌ Error getting notification preferences: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}