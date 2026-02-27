package com.luca.shared.model

data class Item(
    val id: String = "",
    val itemName: String = "",
    val price: Long = 0L,
    val quantity: Int = 1,
    val memberNames: List<String> = emptyList(),
    val taxPercentage: Double = 0.0,
    val discountAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
