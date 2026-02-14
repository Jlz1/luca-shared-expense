package com.example.luca.data.model

import com.google.gson.annotations.SerializedName

data class ScanResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: ReceiptData? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("debug")
    val debug: DebugInfo? = null
)

data class ReceiptData(
    @SerializedName("items")
    val items: List<ReceiptItem> = emptyList(),

    @SerializedName("summary")
    val summary: ReceiptSummary? = null,  // Make nullable

    @SerializedName("status")
    val status: String? = null
)

data class ReceiptItem(
    @SerializedName("name")
    val name: String,

    @SerializedName("qty")
    val qty: String,  // Changed from Int to String (HF returns "2" not 2)

    @SerializedName("price")
    val price: String? = null,  // HF uses "price" not "unit_price"

    @SerializedName("unit_price")
    val unitPrice: String? = null,  // Keep for backward compatibility

    @SerializedName("line_total")
    val lineTotal: String? = null
)

data class ReceiptSummary(
    @SerializedName("subtotal")
    val subtotal: String? = null,  // Changed to String and nullable

    @SerializedName("total_discount")
    val totalDiscount: String? = null,

    @SerializedName("tax")
    val tax: String? = null,

    @SerializedName("service")
    val service: String? = null,

    @SerializedName("service_charge")
    val serviceCharge: String? = null,  // HF uses "service_charge"

    @SerializedName("grand_total")
    val grandTotal: String? = null,

    @SerializedName("calculated_total")
    val calculatedTotal: String? = null,

    @SerializedName("total")
    val total: String? = null,  // HF uses "total" directly

    @SerializedName("diff")
    val diff: String? = null
)

data class DebugInfo(
    @SerializedName("words_detected")
    val wordsDetected: Int,

    @SerializedName("lines_after_filter")
    val linesAfterFilter: Int,

    @SerializedName("raw_text")
    val rawText: String
)

