package com.example.luca.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.luca.data.api.ScanApiClient
import com.example.luca.data.model.ScanResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

class ScanRepository {
    private val api = ScanApiClient.service

    suspend fun uploadReceipt(imageFile: File): Result<ScanResponse> {
        return try {
            // Compress image sebelum upload
            val compressedBytes = compressImage(imageFile)

            val requestFile = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = api.scanReceipt(body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Upload gagal: ${response.message()}"))
            }
        } catch (e: Exception) {
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