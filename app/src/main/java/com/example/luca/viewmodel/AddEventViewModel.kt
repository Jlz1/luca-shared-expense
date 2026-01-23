package com.example.luca.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.data.LucaRepository
import com.example.luca.model.BankAccountData
import com.example.luca.model.Contact
import com.example.luca.model.Event
import com.example.luca.model.ParticipantData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddEventViewModel : ViewModel() {
    private val repository: LucaRepository = LucaFirebaseRepository()

    // --- STATE INPUT ---
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri = _selectedImageUri.asStateFlow()

    // --- STATE PARTICIPANTS ---

    private val _availableContacts = MutableStateFlow<List<Contact>>(emptyList())
    val availableContacts = _availableContacts.asStateFlow()

    private val _selectedParticipants = MutableStateFlow<List<Contact>>(emptyList())
    val selectedParticipants = _selectedParticipants.asStateFlow()

    // Loading & Success
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    init {
        // Load data awal
        fetchContacts()
        fetchCurrentUser() // <--- BARU: Load user sendiri sebagai default participant
    }

    // Fungsi untuk mengambil profile user yang sedang login
    private fun fetchCurrentUser() {
        viewModelScope.launch {
            val userContact = repository.getCurrentUserAsContact()
            if (userContact != null) {
                // Tambahkan user sendiri ke list selected participants
                // Kita gunakan toMutableList agar aman
                val currentList = _selectedParticipants.value.toMutableList()
                // Cek agar tidak duplikat (safety check)
                if (currentList.none { it.id == userContact.id }) {
                    currentList.add(0, userContact) // Tambah di paling depan
                    _selectedParticipants.value = currentList
                }
            }
        }
    }

    private fun fetchContacts() {
        viewModelScope.launch {
            repository.getContactsFlow().collect { contacts ->
                _availableContacts.value = contacts
            }
        }
    }

    // Input Actions
    fun onTitleChange(newTitle: String) { _title.value = newTitle }
    fun onLocationChange(newLocation: String) { _location.value = newLocation }
    fun onDateChange(newDate: String) { _date.value = newDate }
    fun onImageSelected(uri: Uri?) { _selectedImageUri.value = uri }

    // Participant Actions
    fun updateSelectedParticipants(newSelection: List<Contact>) {
        // PERBAIKAN LOGIC:
        // Saat user memilih teman dari Overlay, kita harus pastikan User Sendiri (Host)
        // tidak hilang dari list.

        viewModelScope.launch {
            val host = repository.getCurrentUserAsContact()

            val finalList = if (host != null) {
                // Ambil semua yang dipilih user, tapi filter host lama biar ga dobel
                val selectionWithoutHost = newSelection.filter { it.id != host.id }
                // Gabungkan: [Host] + [Pilihan User]
                listOf(host) + selectionWithoutHost
            } else {
                newSelection
            }

            _selectedParticipants.value = finalList
        }
    }

    fun addNewContact(name: String, phone: String, avatarName: String, banks: List<BankAccountData>) {
        viewModelScope.launch {
            val newContact = Contact(
                name = name,
                phoneNumber = phone,
                avatarName = avatarName,
                bankAccounts = banks,
                description = ""
            )
            repository.addContact(newContact)
        }
    }

    fun saveEvent() {
        if (_title.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            var imageUrl = ""
            val currentUri = _selectedImageUri.value
            if (currentUri != null) {
                imageUrl = repository.uploadEventImage(currentUri) ?: ""
            }

            val participantsData = _selectedParticipants.value.map { contact ->
                ParticipantData(
                    name = contact.name,
                    avatarName = contact.avatarName
                )
            }

            val newEvent = Event(
                id = "",
                title = _title.value,
                location = _location.value,
                date = _date.value,
                imageUrl = imageUrl,
                participants = participantsData
            )

            val result = repository.createEvent(newEvent)

            _isSuccess.value = result.isSuccess
            _isLoading.value = false
        }
    }

    fun resetState() {
        _title.value = ""
        _location.value = ""
        _date.value = ""
        _selectedImageUri.value = null
        _selectedParticipants.value = emptyList()
        _isSuccess.value = false
        _isLoading.value = false
        // Reload user sendiri setelah reset
        fetchCurrentUser()
    }
}