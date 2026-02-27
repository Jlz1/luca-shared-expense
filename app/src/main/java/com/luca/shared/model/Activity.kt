package com.luca.shared.model

data class Activity(
    val id: String = "",
    val eventId: String = "",
    val title: String = "",
    val payerName: String = "",
    val amount: String = "",
    val category: String = "",
    val categoryColorHex: String = "#FFCC80",
    val participants: List<ParticipantData> = emptyList(),
    val paidBy: ParticipantData? = null,

    // Tax and Discount
    val taxPercentage: Double = 0.0,
    val discountAmount: Double = 0.0,

    // Timestamp untuk sorting (newest first)
    val createdAt: Long = System.currentTimeMillis()
)