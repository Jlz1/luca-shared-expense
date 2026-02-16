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
    val summary: ReceiptSummary? = null,

    // Direct fields (HuggingFace format) - at same level as items
    @SerializedName("subtotal")
    val subtotal: Any? = null,

    @SerializedName("tax")
    val tax: Any? = null,

    @SerializedName("service")
    val service: Any? = null,

    @SerializedName("service_charge")
    val serviceCharge: Any? = null,

    @SerializedName("total")
    val total: Any? = null,

    @SerializedName("total_discount")
    val totalDiscount: Any? = null,

    @SerializedName("grand_total")
    val grandTotal: Any? = null,

    @SerializedName("status")
    val status: String? = null
)

data class ReceiptItem(
    @SerializedName("name")
    val name: String,

    @SerializedName("qty")
    val qty: Any = 1,  // Accept both Int and String

    @SerializedName("price")
    val price: Any? = null,  // Accept Int or String

    @SerializedName("unit_price")
    val unitPrice: Any? = null,

    @SerializedName("line_total")
    val lineTotal: Any? = null
)

data class ReceiptSummary(
    @SerializedName("subtotal")
    val subtotal: Any? = null,  // Accept both Int and String

    @SerializedName("total_discount")
    val totalDiscount: Any? = null,

    @SerializedName("tax")
    val tax: Any? = null,

    @SerializedName("service")
    val service: Any? = null,

    @SerializedName("service_charge")
    val serviceCharge: Any? = null,

    @SerializedName("grand_total")
    val grandTotal: Any? = null,

    @SerializedName("calculated_total")
    val calculatedTotal: Any? = null,

    @SerializedName("total")
    val total: Any? = null,

    @SerializedName("diff")
    val diff: Any? = null
)

data class DebugInfo(
    @SerializedName("words_detected")
    val wordsDetected: Int,

    @SerializedName("lines_after_filter")
    val linesAfterFilter: Int,

    @SerializedName("raw_text")
    val rawText: String
)

