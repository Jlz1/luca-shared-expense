package com.example.luca.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ScanApiClient {
    // GANTI URL ini dengan Hugging Face Space kamu yang udah running!
    private const val BASE_URL = "https://lucashared-luca-shared-expense.hf.space/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // INI YANG KURANG! Expose service supaya bisa dipanggil dari Repository
    val service: ScanApiService by lazy {
        retrofit.create(ScanApiService::class.java)
    }
}