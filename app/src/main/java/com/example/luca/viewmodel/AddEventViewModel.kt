package com.example.luca.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.data.LucaRepository
import com.example.luca.model.Event
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AddEventViewModel : ViewModel() {
    // Repository & Auth Init
    private val repository: LucaRepository = LucaFirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    // --- STATE ---

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
            _participants.value = _participants.value + name
        }
    }

    fun saveEvent() {
        // Validasi sederhana
        if (_title.value.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Cek User Login (WAJIB)
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid

                // 2. Upload Gambar (Jika User memilih gambar)
                var imageUrl = ""
                val currentUri = _selectedImageUri.value

                // Proses upload hanya jika ada gambar
                if (currentUri != null) {
                    val uploadedUrl = repository.uploadEventImage(currentUri)
                    if (uploadedUrl != null) imageUrl = uploadedUrl
                }

                // 3. Buat Objek Event
                val newEvent = Event(
                    id = UUID.randomUUID().toString(),
                    title = _title.value,
                    location = _location.value,
                    date = _date.value,
                    imageUrl = imageUrl,
                    participantAvatars = _participants.value
                )

                // 4. Simpan ke Firestore (FIX: Kirim userId dan event)
                val success = repository.createEvent(userId, newEvent)

                _isSuccess.value = success
            } else {
                // User belum login (harusnya tidak terjadi di flow ini, tapi buat jaga-jaga)
                _isSuccess.value = false
            }

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
        _isLoading.value = false
    }
}