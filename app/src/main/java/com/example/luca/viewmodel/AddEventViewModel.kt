package com.example.luca.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.data.LucaRepository
import com.example.luca.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AddEventViewModel : ViewModel() {
    private val repository: LucaRepository = LucaFirebaseRepository()

    // State Input
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    // State Gambar
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri = _selectedImageUri.asStateFlow()

    // State Partisipan (List Nama)
    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants = _participants.asStateFlow()

    // State Status
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    // --- EVENTS / ACTIONS ---

    fun onTitleChange(newTitle: String) { _title.value = newTitle }
    fun onLocationChange(newLocation: String) { _location.value = newLocation }
    fun onDateChange(newDate: String) { _date.value = newDate }
    fun onImageSelected(uri: Uri?) { _selectedImageUri.value = uri }

    fun addParticipant(name: String) {
        if (name.isNotBlank()) {
            // Tambah nama baru ke list yang sudah ada
            _participants.value = _participants.value + name
        }
    }

    fun saveEvent() {
        if (_title.value.isEmpty()) return // Validasi simple

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Upload Gambar (Jika User memilih gambar)
            var imageUrl = ""
            val currentUri = _selectedImageUri.value
            if (currentUri != null) {
                val uploadedUrl = repository.uploadEventImage(currentUri)
                if (uploadedUrl != null) imageUrl = uploadedUrl
            }

            // 2. Buat Objek Event
            val newEvent = Event(
                id = UUID.randomUUID().toString(),
                title = _title.value,
                location = _location.value,
                date = _date.value,
                imageUrl = imageUrl,
                participantAvatars = _participants.value // Simpan list nama teman
            )

            // 3. Simpan ke Firestore
            val success = repository.createEvent(newEvent)
            _isSuccess.value = success
            _isLoading.value = false
        }
    }

    fun resetState() {
        _title.value = ""
        _location.value = ""
        _date.value = ""
        _selectedImageUri.value = null
        _participants.value = emptyList()
        _isSuccess.value = false
    }
}