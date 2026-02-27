package com.luca.shared.model

// Helper model untuk menyimpan data partisipan di dalam event
data class ParticipantData(
    val name: String = "",
    val avatarName: String = "avatar_1"
)

data class Event(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val date: String = "",
    val imageUrl: String = "", // Menyimpan URL download dari Firebase Storage

    // UPDATE: Menggunakan List<ParticipantData>
    val participants: List<ParticipantData> = emptyList(),

    // Field legacy (bisa dibiarkan kosong, atau dipakai sebagai backup)
    val participantAvatars: List<String> = emptyList(),

    // Timestamp untuk sorting (newest first)
    val createdAt: Long = System.currentTimeMillis()
)