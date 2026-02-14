package com.example.luca.data.api

import com.example.luca.data.model.ScanResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ScanApiService {
    @Multipart
    @POST("scan")  // ← HuggingFace Space endpoint
    suspend fun scanReceipt(
        @Part file: MultipartBody.Part
    ): Response<ScanResponse>
}