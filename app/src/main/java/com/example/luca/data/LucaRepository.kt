package com.example.luca.data

import com.example.luca.model.Activity
import com.example.luca.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

// 1. KONTRAK (Interface)
interface LucaRepository {
    suspend fun getAllEvents(): List<Event>
    suspend fun getEventById(id: String): Event?
    suspend fun getActivitiesByEventId(eventId: String): List<Activity>
}

// 2. IMPLEMENTASI FIREBASE (Live Data)
class LucaFirebaseRepository : LucaRepository {

    private val db = FirebaseFirestore.getInstance()

    // Pastikan nama collection di Firebase Console sama persis: "events" dan "activities"
    private val eventsCollection = db.collection("events")
    private val activitiesCollection = db.collection("activities")

    override suspend fun getAllEvents(): List<Event> {
        return try {
            val snapshot = eventsCollection.get().await()
            snapshot.toObjects<Event>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEventById(id: String): Event? {
        return try {
            // Query cari dokumen yang field 'id'-nya sama dengan parameter
            val snapshot = eventsCollection.whereEqualTo("id", id).get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(Event::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getActivitiesByEventId(eventId: String): List<Activity> {
        return try {
            val snapshot = activitiesCollection
                .whereEqualTo("eventId", eventId)
                .get()
                .await()
            snapshot.toObjects<Activity>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}