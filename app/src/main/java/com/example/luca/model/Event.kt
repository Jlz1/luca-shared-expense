package com.example.luca.model

data class Event(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val participantAvatars: List<String> = emptyList()
)