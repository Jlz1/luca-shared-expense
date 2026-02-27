package com.luca.shared.data.repository

import android.util.Log
import com.luca.shared.data.api.EmailApiService
import com.luca.shared.data.api.EmailParams
import com.luca.shared.data.api.EmailRequest

class EmailRepository {

    // Konfigurasi Akun EmailJS Kamu
    private val SERVICE_ID = "service_5htnm6l"
    private val TEMPLATE_ID = "template_ecpplv9"
    private val PUBLIC_KEY = "5c07CTBPmjbXOFhKJ"

    suspend fun sendOtpToEmail(emailTujuan: String, namaUser: String, otpCode: String): Boolean {
        return try {
            // 1. Siapkan Parameter Data (Nama, Email, OTP)
            val params = EmailParams(
                toName = namaUser,
                toEmail = emailTujuan,
                otp = otpCode
            )

            // 2. Siapkan Request Body Lengkap
            val request = EmailRequest(
                serviceId = SERVICE_ID,
                templateId = TEMPLATE_ID,
                publicKey = PUBLIC_KEY,
                templateParams = params
            )

            // 3. Panggil API via Retrofit
            // Kita memanggil instance 'api' dari object 'EmailApiService'
            val response = EmailApiService.api.sendEmail(request)

            if (response.isSuccessful) {
                Log.d("EmailRepository", "SUKSES: Email terkirim ke $emailTujuan")
                true
            } else {
                // Log error body untuk debugging jika gagal
                val errorMsg = response.errorBody()?.string() ?: "Unknown Error"
                Log.e("EmailRepository", "GAGAL: ${response.code()} - $errorMsg")
                false
            }
        } catch (e: Exception) {
            Log.e("EmailRepository", "ERROR KONEKSI: ${e.message}")
            false
        }
    }
}