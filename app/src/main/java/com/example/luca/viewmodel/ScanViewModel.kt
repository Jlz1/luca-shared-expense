package com.example.luca.ui.viewmodel

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

    private val _scanState = MutableStateFlow("Menunggu Scan...")
    val scanState: StateFlow<String> = _scanState

    private val _parsedReceiptData = MutableStateFlow<ParsedReceiptData?>(null)
    val parsedReceiptData: StateFlow<ParsedReceiptData?> = _parsedReceiptData

    fun uploadImage(imageFile: File) {
        viewModelScope.launch {
            _scanState.value = "Sedang menganalisa struk...\n(Ini bisa memakan waktu 30-60 detik untuk pertama kali)"

            val result = repository.uploadReceipt(imageFile)

            result.onSuccess { response ->
                when {
                    response.status == "success" && response.data != null -> {
                        val data = response.data

                        // Convert the new API response to ParsedReceiptData format
                        val items = data.items.map { item ->
                            ParsedReceiptItem(
                                itemName = item.name,
                                itemPrice = item.lineTotal.toDouble(),
                                itemQuantity = item.qty,
                                itemDiscount = 0.0,
                                itemTax = 0.0
                            )
                        }

                        val parsedData = ParsedReceiptData(
                            items = items,
                            subtotal = data.summary.subtotal.toDouble(),
                            tax = data.summary.tax.toDouble(),
                            discount = data.summary.totalDiscount.toDouble(),
                            totalBill = data.summary.grandTotal.toDouble(),
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