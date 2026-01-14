package com.example.luca.model

data class Event(
    val id: String,
    val title: String,
    val location: String,
    val date: String,
    val imageUrl: String = "", // Nanti buat load gambar
    val participantAvatars: List<String> // List kode avatar ("avatar_1", "avatar_2")
)