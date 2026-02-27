package com.luca.shared.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luca.shared.data.LucaFirebaseRepository
import com.luca.shared.data.LucaRepository
import com.luca.shared.model.Activity
import com.luca.shared.model.Contact
import com.luca.shared.model.ParticipantData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LucaRepository = LucaFirebaseRepository()

    // Event ID
    private val _eventId = MutableStateFlow("")
    val eventId = _eventId.asStateFlow()

    // Form Inputs
    private val _titleInput = MutableStateFlow("")
    val titleInput = _titleInput.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedParticipants = MutableStateFlow<List<Contact>>(emptyList())
    val selectedParticipants = _selectedParticipants.asStateFlow()

    private val _selectedPayer = MutableStateFlow<Contact?>(null)
    val selectedPayer = _selectedPayer.asStateFlow()

    // Event Participants (dari Firebase Event document)
    private val _eventParticipants = MutableStateFlow<List<Contact>>(emptyList())
    val eventParticipants = _eventParticipants.asStateFlow()

    // Loading & Success
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    // Created Activity ID - untuk navigation setelah create
    private val _createdActivityId = MutableStateFlow("")
    val createdActivityId = _createdActivityId.asStateFlow()

    fun setEventId(newEventId: String) {
        _eventId.value = newEventId
    }

    fun loadEventParticipants(eventData: com.luca.shared.model.Event) {
        // Konversi ParticipantData dari Event menjadi Contact
        val participants = eventData.participants.map { p ->
            Contact(
                name = p.name,
                avatarName = if (p.avatarName.isNotBlank()) p.avatarName else "avatar_1"
            )
        }
        _eventParticipants.value = participants
    }

    fun onTitleChange(newTitle: String) {
        _titleInput.value = newTitle
    }

    fun onCategoryChange(newCategory: String) {
        _selectedCategory.value = newCategory
    }

    fun updateSelectedParticipants(participants: List<Contact>) {
        _selectedParticipants.value = participants
    }

    fun onPayerChange(payer: Contact) {
        _selectedPayer.value = payer
    }

    fun saveActivity() {
        val eventIdValue = _eventId.value
        if (eventIdValue.isEmpty() || _titleInput.value.isBlank()) {
            android.util.Log.e("AddActivityViewModel", "saveActivity failed: eventId or title is empty")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val participantsData = _selectedParticipants.value.map { contact ->
                ParticipantData(
                    name = contact.name,
                    avatarName = contact.avatarName
                )
            }

            val paidByData = _selectedPayer.value?.let { payer ->
                ParticipantData(
                    name = payer.name,
                    avatarName = payer.avatarName
                )
            }

            val categoryColorHex = getCategoryColorHex(_selectedCategory.value)

            val newActivity = Activity(
                title = _titleInput.value,
                category = _selectedCategory.value,
                categoryColorHex = categoryColorHex,
                participants = participantsData,
                paidBy = paidByData,
                payerName = _selectedPayer.value?.name ?: ""
            )

            android.util.Log.d("AddActivityViewModel", "Creating activity: ${newActivity.title}")
            val result = repository.createActivity(eventIdValue, newActivity)

            if (result.isSuccess) {
                val activityId = result.getOrNull() ?: ""
                android.util.Log.d("AddActivityViewModel", "Activity created successfully! ID: $activityId")
                _createdActivityId.value = activityId
                _isSuccess.value = true
            } else {
                android.util.Log.e("AddActivityViewModel", "Failed to create activity: ${result.exceptionOrNull()?.message}")
                _isSuccess.value = false
            }
            _isLoading.value = false
        }
    }

    fun resetState() {
        _titleInput.value = ""
        _selectedCategory.value = ""
        _selectedParticipants.value = emptyList()
        _selectedPayer.value = null
        _isSuccess.value = false
        _isLoading.value = false
        _createdActivityId.value = ""
    }

    private fun getCategoryColorHex(categoryName: String): String {
        return when (categoryName) {
            "Food" -> "#FFA726"
            "Shopping" -> "#AB47BC"
            "Transportation" -> "#42A5F5"
            "Entertainment" -> "#EC407A"
            "Others" -> "#FFCC80"
            else -> "#FFCC80"
        }
    }
}
