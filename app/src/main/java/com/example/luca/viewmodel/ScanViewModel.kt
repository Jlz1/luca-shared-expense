package com.example.luca.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ScanViewModel : ViewModel() {
    private val repository = ScanRepository()

    private val _scanState = MutableStateFlow("Menunggu Scan...")
    val scanState: StateFlow<String> = _scanState

    fun uploadImage(imageFile: File) {
        viewModelScope.launch {
            _scanState.value = "Sedang menganalisa struk...\n(Ini bisa memakan waktu 30-60 detik untuk pertama kali)"

            val result = repository.uploadReceipt(imageFile)

            result.onSuccess { response ->
                when {
                    response.status == "success" && !response.filteredText.isNullOrEmpty() -> {
                        _scanState.value = response.filteredText
                    }
                    response.status == "success" && response.filteredText.isNullOrEmpty() -> {
                        _scanState.value = "❌ Tidak ada text terdeteksi.\nCoba ambil foto lagi dengan pencahayaan lebih baik."
                    }
                    else -> {
                        _scanState.value = "❌ Error: ${response.message ?: "Unknown error"}"
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
            }
        }
    }

    fun resetScan() {
        _scanState.value = "Menunggu Scan..."
    }
}