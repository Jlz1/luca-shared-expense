package com.example.luca.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.repository.ScanRepository
import com.example.luca.model.ParsedReceiptData
import com.example.luca.model.ParsedReceiptItem
import com.example.luca.utils.ReceiptParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ScanViewModel : ViewModel() {
    private val repository = ScanRepository()
    private val TAG = "ScanViewModel"

    // Helper function untuk parse Indonesian number format (accepts String or Int)
    private fun parseIndonesianNumber(value: Any?): Double {
        if (value == null) {
            Log.d(TAG, "parseIndonesianNumber: null value")
            return 0.0
        }

        val strValue = when (value) {
            is String -> value
            is Int -> value.toString()
            is Long -> value.toString()
            is Double -> return value
            is Float -> return value.toDouble()
            else -> value.toString()
        }

        if (strValue.isEmpty() || strValue == "0") {
            Log.d(TAG, "parseIndonesianNumber: null/empty value")
            return 0.0
        }

        // "56.936" → remove all dots → "56936" → 56936.0
        val cleaned = strValue.replace(".", "").replace(",", ".")
        val result = cleaned.toDoubleOrNull() ?: 0.0
        Log.d(TAG, "parseIndonesianNumber: '$value' -> '$cleaned' -> $result")
        return result
    }

    private val _scanState = MutableStateFlow("Menunggu Scan...")
    val scanState: StateFlow<String> = _scanState

    private val _parsedReceiptData = MutableStateFlow<ParsedReceiptData?>(null)
    val parsedReceiptData: StateFlow<ParsedReceiptData?> = _parsedReceiptData

    fun uploadImage(imageFile: File) {
        viewModelScope.launch {
            _scanState.value = "Sedang menganalisa struk...\n(Ini bisa memakan waktu 30-60 detik untuk pertama kali)"

            val result = repository.uploadReceipt(imageFile)

            result.onSuccess { response ->
                // Log the entire response object
                Log.d(TAG, "=== FULL RESPONSE ===")
                Log.d(TAG, "Status: ${response.status}")
                Log.d(TAG, "Message: ${response.message}")
                Log.d(TAG, "Data: ${response.data}")
                Log.d(TAG, "Data.summary: ${response.data?.summary}")
                Log.d(TAG, "Data.items: ${response.data?.items}")

                when {
                    response.status == "success" && response.data != null -> {
                        val data = response.data

                        // Convert the new API response to ParsedReceiptData format
                        // Note: Fields are now String (or nullable), not Int
                        val items = data.items.map { item ->
                            // Parse price from Any (Int or String) to Double
                            val price = parseIndonesianNumber(item.price ?: item.lineTotal ?: 0)

                            // Parse qty from Any to Int
                            val qty = when (item.qty) {
                                is String -> (item.qty as String).toIntOrNull() ?: 1
                                is Int -> item.qty as Int
                                else -> 1
                            }

                            ParsedReceiptItem(
                                itemName = item.name,
                                itemPrice = price,
                                itemQuantity = qty,
                                itemDiscount = 0.0,
                                itemTax = 0.0
                            )
                        }

                        // Parse summary values using helper function
                        val summary = data.summary
                        Log.d(TAG, "=== SUMMARY DEBUG ===")
                        Log.d(TAG, "Summary object: $summary")

                        // Check if fields are at summary level or direct at data level
                        val subtotalRaw = summary?.subtotal ?: data.subtotal
                        val taxRaw = summary?.tax ?: data.tax
                        val serviceRaw = summary?.service ?: summary?.serviceCharge ?: data.service ?: data.serviceCharge
                        val discountRaw = summary?.totalDiscount ?: data.totalDiscount
                        val totalRaw = summary?.total ?: summary?.grandTotal ?: data.total ?: data.grandTotal

                        Log.d(TAG, "Summary.subtotal: ${summary?.subtotal}, Data.subtotal: ${data.subtotal}")
                        Log.d(TAG, "Summary.tax: ${summary?.tax}, Data.tax: ${data.tax}")
                        Log.d(TAG, "Summary.service: ${summary?.service}, Data.service: ${data.service}")
                        Log.d(TAG, "Summary.serviceCharge: ${summary?.serviceCharge}, Data.serviceCharge: ${data.serviceCharge}")
                        Log.d(TAG, "Summary.total: ${summary?.total}, Data.total: ${data.total}")

                        val subtotal = parseIndonesianNumber(subtotalRaw)
                        val tax = parseIndonesianNumber(taxRaw)
                        val serviceCharge = parseIndonesianNumber(serviceRaw)
                        val discount = parseIndonesianNumber(discountRaw)
                        val total = parseIndonesianNumber(totalRaw)

                        Log.d(TAG, "Raw values - subtotal: $subtotalRaw, tax: $taxRaw, service: $serviceRaw, total: $totalRaw")
                        Log.d(TAG, "✓ Parsed: subtotal=$subtotal, tax=$tax, service=$serviceCharge, total=$total")

                        val parsedData = ParsedReceiptData(
                            items = items,
                            subtotal = subtotal,
                            tax = tax,
                            serviceCharge = serviceCharge,
                            discount = discount,
                            totalBill = total,
                            rawText = response.debug?.rawText ?: ""
                        )

                        _parsedReceiptData.value = parsedData
                        _scanState.value = "✅ ${data.items.size} item berhasil dipindai!\nStatus: ${data.status}"
                    }
                    response.status == "success" && response.data?.items?.isEmpty() == true -> {
                        _scanState.value = "❌ Tidak ada item terdeteksi.\nCoba ambil foto lagi dengan pencahayaan lebih baik."
                        _parsedReceiptData.value = null
                    }
                    else -> {
                        _scanState.value = "❌ Error: ${response.message ?: "Unknown error"}"
                        _parsedReceiptData.value = null
                    }
                }
            }.onFailure { exception ->
                val errorMsg = when {
                    exception.message?.contains("timeout", ignoreCase = true) == true ->
                        "⏱️ Timeout: Server terlalu lama merespon.\nCoba lagi dalam beberapa detik."
                    exception.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                        "📡 Tidak ada koneksi internet.\nCek koneksi kamu."
                    else ->
                        "❌ Error: ${exception.message ?: "Gagal koneksi ke server"}"
                }
                _scanState.value = errorMsg
                _parsedReceiptData.value = null
            }
        }
    }

    fun resetScan() {
        _scanState.value = "Menunggu Scan..."
        _parsedReceiptData.value = null
    }
}