package com.example.luca.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ScanViewModel : ViewModel() {
    // Panggil Repository yang sudah kita buat tadi
    private val repository = ScanRepository()

    // Variable buat nampung status (Loading, Hasil Teks, atau Error)
    private val _scanState = MutableStateFlow<String>("Menunggu Scan...")
    val scanState = _scanState.asStateFlow()

    fun uploadImage(file: File) {
        viewModelScope.launch {
            _scanState.value = "Sedang Mengupload... Mohon Tunggu ⏳"

            // Proses Upload ke Cloud Run
            val result = repository.uploadReceipt(file)

            result.onSuccess { response ->
                // Kalau sukses, ambil teks hasil filter
                val teksStruk = response.filteredText ?: "Struk kosong / Gagal baca"
                _scanState.value = teksStruk
            }

            result.onFailure { error ->
                // Kalau gagal (internet mati / server error)
                _scanState.value = "Error: ${error.message}"
            }
        }
    }
}