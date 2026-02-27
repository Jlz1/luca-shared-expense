package com.luca.shared.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.luca.shared.data.api.ScanApiClient
import com.luca.shared.data.model.ScanResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

class ScanRepository {
    private val api = ScanApiClient.service
    private val TAG = "ScanRepository"

    suspend fun uploadReceipt(imageFile: File): Result<ScanResponse> {
        return try {
            Log.d(TAG, "Starting upload for file: ${imageFile.name}, size: ${imageFile.length()} bytes")

            // Compress image sebelum upload
            val compressedBytes = compressImage(imageFile)
            Log.d(TAG, "Image compressed to ${compressedBytes.size} bytes")

            val requestFile = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            Log.d(TAG, "Sending request to API...")
            val response = api.scanReceipt(body)

            Log.d(TAG, "Response code: ${response.code()}, Success: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d(TAG, "Upload successful!")
                Log.d(TAG, "=== RAW RESPONSE JSON ===")
                Log.d(TAG, "Response body: $responseBody")
                Log.d(TAG, "Response.data: ${responseBody.data}")
                Log.d(TAG, "Response.data.summary: ${responseBody.data?.summary}")

                // Log raw JSON string
                val rawJsonString = response.raw().request.url.toString()
                Log.d(TAG, "Request URL: $rawJsonString")
                Log.d(TAG, "Response message: ${response.message()}")
                Log.d(TAG, "Response headers: ${response.headers()}")

                Result.success(responseBody)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error details"
                val errorMsg = "Upload gagal [${response.code()}]: ${response.message()} - $errorBody"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during upload: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Compress image to max 1MB
    private fun compressImage(file: File): ByteArray {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        // Resize jika terlalu besar
        val maxSize = 1920 // max width/height
        val ratio = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        val resizedBitmap = if (ratio < 1) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        // Compress quality
        val outputStream = ByteArrayOutputStream()
        var quality = 85
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // Turunin quality kalau masih > 1MB
        while (outputStream.size() > 1_000_000 && quality > 30) {
            outputStream.reset()
            quality -= 10
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        bitmap.recycle()
        if (resizedBitmap != bitmap) resizedBitmap.recycle()

        return outputStream.toByteArray()
    }
}