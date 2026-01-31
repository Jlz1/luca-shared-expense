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
            _scanState.value = "Sedang menganalisa struk..."

            val result = repository.uploadReceipt(imageFile)

            result.onSuccess { response ->
                if (response.status == "success") {
                    _scanState.value = response.filteredText ?: "Tidak ada text terdeteksi"
                } else {
                    _scanState.value = "Error: ${response.message ?: "Unknown error"}"
                }
            }.onFailure { exception ->
                _scanState.value = "Error: ${exception.message ?: "Gagal koneksi ke server"}"
            }
        }
    }
}