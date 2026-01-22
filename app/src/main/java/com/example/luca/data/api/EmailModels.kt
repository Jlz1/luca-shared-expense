package com.example.luca.data.api

import com.google.gson.annotations.SerializedName

data class EmailRequest(
    @SerializedName("service_id") val serviceId: String,
    @SerializedName("template_id") val templateId: String,
    @SerializedName("user_id") val publicKey: String,
    @SerializedName("template_params") val templateParams: EmailParams
)

data class EmailParams(
    @SerializedName("to_name") val toName: String,
    @SerializedName("to_email") val toEmail: String,
    @SerializedName("otp") val otp: String
)