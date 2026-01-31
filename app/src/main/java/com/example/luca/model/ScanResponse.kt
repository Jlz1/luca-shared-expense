package com.example.luca.data.model

import com.google.gson.annotations.SerializedName

data class ScanResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("raw_text")
    val rawText: String? = null,

    @SerializedName("filtered_text")
    val filteredText: String? = null,

    @SerializedName("message")
    val message: String? = null
)