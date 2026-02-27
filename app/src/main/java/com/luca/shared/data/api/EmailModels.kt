package com.luca.shared.data.api

import com.google.gson.annotations.SerializedName

// Model utama untuk Body Request ke API EmailJS
data class EmailRequest(
    @SerializedName("service_id")
    val serviceId: String,

    @SerializedName("template_id")
    val templateId: String,

    @SerializedName("user_id") // EmailJS menyebut Public Key sebagai user_id di API
    val publicKey: String,

    @SerializedName("template_params")
    val templateParams: EmailParams
)

// Model parameter data yang akan muncul di dalam template Email
data class EmailParams(
    @SerializedName("to_name") // Harus sama dengan {{to_name}} di Dashboard EmailJS
    val toName: String,

    @SerializedName("to_email") // Harus sama dengan {{to_email}} di Dashboard EmailJS
    val toEmail: String,

    @SerializedName("otp") // Harus sama dengan {{otp}} di Dashboard EmailJS
    val otp: String
)