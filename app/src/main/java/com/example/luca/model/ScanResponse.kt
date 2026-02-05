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
    val items: List<ReceiptItem>,

    @SerializedName("summary")
    val summary: ReceiptSummary,

    @SerializedName("status")
    val status: String
)

data class ReceiptItem(
    @SerializedName("name")
    val name: String,

    @SerializedName("qty")
    val qty: Int,

    @SerializedName("unit_price")
    val unitPrice: Int,

    @SerializedName("line_total")
    val lineTotal: Int
)

data class ReceiptSummary(
    @SerializedName("subtotal")
    val subtotal: Int,

    @SerializedName("total_discount")
    val totalDiscount: Int,

    @SerializedName("tax")
    val tax: Int,

    @SerializedName("service")
    val service: Int,

    @SerializedName("grand_total")
    val grandTotal: Int,

    @SerializedName("calculated_total")
    val calculatedTotal: Int,

    @SerializedName("diff")
    val diff: Int
)

data class DebugInfo(
    @SerializedName("words_detected")
    val wordsDetected: Int,

    @SerializedName("lines_after_filter")
    val linesAfterFilter: Int,

    @SerializedName("raw_text")
    val rawText: String
)

