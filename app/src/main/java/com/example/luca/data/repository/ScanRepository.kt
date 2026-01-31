package com.example.luca.data.repository

import com.example.luca.data.api.ScanApiClient
import com.example.luca.data.model.ScanResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ScanRepository {
    private val api = ScanApiClient.service

    suspend fun uploadReceipt(imageFile: File): Result<ScanResponse> {
        return try {
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = api.scanReceipt(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}