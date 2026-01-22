package com.example.luca.data.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailApiService {
    @POST("api/v1.0/email/send")
    suspend fun sendEmail(@Body request: EmailRequest): Response<Unit>

    companion object {
        private const val BASE_URL = "https://api.emailjs.com/"

        val api: EmailApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EmailApiService::class.java)
        }
    }
}