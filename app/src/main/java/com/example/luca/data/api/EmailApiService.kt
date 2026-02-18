package com.example.luca.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Interface Definisi Endpoint API
interface EmailApi {
    @POST("email/send") // Endpoint relative terhadap Base URL
    suspend fun sendEmail(@Body request: EmailRequest): Response<ResponseBody>
}

// 2. Singleton Object untuk Koneksi Retrofit
object EmailApiService {
    // Base URL resmi EmailJS (Versi 1.0)
    private const val BASE_URL = "https://api.emailjs.com/api/v1.0/"

    val api: EmailApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailApi::class.java)
    }
}