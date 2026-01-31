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
            // 1. Ubah File jadi Multipart (paket yang bisa dikirim via internet)
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            // 2. Panggil API
            val response = api.scanReceipt(body)

            // 3. Cek sukses atau gagal
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