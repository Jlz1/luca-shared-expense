package com.example.luca.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
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

class AddEventViewModel(application: Application) : AndroidViewModel(application) {
    // Menggunakan Application Context untuk Repository (diperlukan untuk kompresi gambar/resource)
    private val repository: LucaRepository = LucaFirebaseRepository(application.applicationContext)

    // --- VARIABEL UNTUK MODE EDIT ---
    private var currentEventId: String? = null // Jika null = Create, Jika ada isi = Edit
    private var existingImageUrl: String = ""  // Menyimpan URL foto lama

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

    // Error Handling
    private val _showParticipantWarning = MutableStateFlow(false)
    val showParticipantWarning = _showParticipantWarning.asStateFlow()

    private val _removedParticipantsInActivity = MutableStateFlow<List<String>>(emptyList())
    val removedParticipantsInActivity = _removedParticipantsInActivity.asStateFlow()

    init {
        // Load data awal
        fetchContacts()
    }

    // --- FUNGSI LOAD DATA UNTUK EDIT ---
    fun loadEventForEdit(eventId: String) {
        // Cegah reload jika ID sama
        if (currentEventId == eventId) return

        currentEventId = eventId
        _isLoading.value = true

        viewModelScope.launch {
            val event = repository.getEventById(eventId)
            if (event != null) {
                // 1. Isi Form Input
                _title.value = event.title
                _location.value = event.location
                _date.value = event.date
                existingImageUrl = event.imageUrl // Simpan URL lama

                // 2. Isi Preview Gambar (Parse URL ke Uri)
                if (event.imageUrl.isNotEmpty()) {
                    _selectedImageUri.value = Uri.parse(event.imageUrl)
                }

                // 3. Mapping Participants (Dari ParticipantData -> Contact)
                val mappedParticipants = event.participants.map { p ->
                    Contact(
                        id = "", // ID tidak krusial untuk display list
                        userId = "",
                        name = p.name,
                        avatarName = p.avatarName,
                        description = "Participant"
                    )
                }
                _selectedParticipants.value = mappedParticipants
            }
            // JANGAN panggil fetchCurrentUser() di sini - participant sudah di-load dari event
            _isLoading.value = false
        }
    }

    // Fungsi untuk mengambil profile user yang sedang login (Dipanggil jika Mode Create)
    fun fetchCurrentUser() {
        // Jangan timpa participant jika kita sedang dalam mode edit
        if (currentEventId != null) return

        viewModelScope.launch {
            val userContact = repository.getCurrentUserAsContact()
            if (userContact != null) {
                val currentList = _selectedParticipants.value.toMutableList()
                // Cek agar tidak duplikat
                if (currentList.none { it.name == userContact.name }) {
                    currentList.add(0, userContact)
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
        _selectedParticipants.value = newSelection
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

            // CEK APAKAH ADA PARTICIPANT YANG DIHAPUS DAN SUDAH MASUK ACTIVITY
            if (currentEventId != null) {
                val participantsInActivities = repository.getParticipantsInActivities(currentEventId!!)
                val currentParticipantNames = _selectedParticipants.value.map { it.name }
                val removedParticipants = participantsInActivities.filter { it !in currentParticipantNames }

                if (removedParticipants.isNotEmpty()) {
                    _removedParticipantsInActivity.value = removedParticipants
                    _showParticipantWarning.value = true
                    _isLoading.value = false
                    return@launch
                }
            }

            // LOGIK IMAGE UPDATE
            var finalImageUrl = existingImageUrl
            val currentUri = _selectedImageUri.value

            if (currentUri != null && currentUri.toString() != existingImageUrl) {
                val uploadedUrl = repository.uploadEventImage(currentUri)
                if (uploadedUrl != null) {
                    finalImageUrl = uploadedUrl
                }
            }

            val participantsData = _selectedParticipants.value.map { contact ->
                ParticipantData(
                    name = contact.name,
                    avatarName = contact.avatarName
                )
            }

            val newEvent = Event(
                id = currentEventId ?: "", // PENTING: Pakai ID lama jika edit, kosong jika baru
                title = _title.value,
                location = _location.value,
                date = _date.value,
                imageUrl = finalImageUrl,
                participants = participantsData
            )

            // Repository createEvent menghandle 'set' data (Create or Update)
            val result = repository.createEvent(newEvent)

            _isSuccess.value = result.isSuccess
            _isLoading.value = false
        }
    }

    fun dismissParticipantWarning() {
        _showParticipantWarning.value = false
    }

    fun resetState() {
        _title.value = ""
        _location.value = ""
        _date.value = ""
        _selectedImageUri.value = null
        _selectedParticipants.value = emptyList()
        currentEventId = null // Reset ID ke mode create
        existingImageUrl = ""
        _isSuccess.value = false
        _isLoading.value = false
        _showParticipantWarning.value = false
        _removedParticipantsInActivity.value = emptyList()
        // Reload user sendiri setelah reset (siap untuk create baru)
        fetchCurrentUser()
    }
}