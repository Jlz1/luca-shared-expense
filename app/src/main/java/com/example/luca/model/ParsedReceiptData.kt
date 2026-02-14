package com.example.luca.model

data class ParsedReceiptData(
    val items: List<ParsedReceiptItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val serviceCharge: Double = 0.0,  // Added
    val discount: Double = 0.0,
    val totalBill: Double = 0.0,
    val rawText: String = ""
)

data class ParsedReceiptItem(
    val itemName: String,
    val itemPrice: Double,
    val itemQuantity: Int = 1,
    val itemDiscount: Double = 0.0,
    val itemTax: Double = 0.0
)

