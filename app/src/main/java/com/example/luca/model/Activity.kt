package com.example.luca.model

data class Activity(
    val id: String = "",
    val eventId: String = "",
    val title: String,
    val payerName: String,
    val amount: String,
    val categoryColorHex: String = "#FFCC80"
)