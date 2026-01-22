package com.example.luca.data.repository

import android.util.Log
import com.example.luca.data.api.EmailApiService
import com.example.luca.data.api.EmailParams
import com.example.luca.data.api.EmailRequest



class EmailRepository {
    private val SERVICE_ID = "service_5htnm6l"
    private val TEMPLATE_ID = "template_ecpplv9"
    private val PUBLIC_KEY = "5c07CTBPmjbXOFhKJ"

    suspend fun sendOtpToEmail(emailTujuan: String, namaUser: String, otpCode: String): Boolean {
        return try {
            val params = EmailParams(
                toName = namaUser,
                toEmail = emailTujuan,
                otp = otpCode
            )

            val request = EmailRequest(
                serviceId = SERVICE_ID,
                templateId = TEMPLATE_ID,
                publicKey = PUBLIC_KEY,
                templateParams = params
            )

            val response = EmailApiService.api.sendEmail(request)

            if (response.isSuccessful) {
                Log.d("EmailRepository", "Email terkirim ke $emailTujuan")
                true
            } else {
                Log.e("EmailRepository", "Gagal kirim: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("EmailRepository", "Error koneksi: ${e.message}")
            false
        }
    }
}