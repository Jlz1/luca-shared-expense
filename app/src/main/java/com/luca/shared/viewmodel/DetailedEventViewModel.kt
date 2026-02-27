package com.luca.shared.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luca.shared.data.LucaRepository
import com.luca.shared.data.LucaFirebaseRepository
import com.luca.shared.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Wrapper State untuk UI
data class UIEventState(
    val title: String = "",
    val location: String = "",
    val date: String = "",
    val imageUrl: String = "",
    // List Avatar Name (String)
    val participantAvatars: List<String> = emptyList()
)

data class UIActivityState(
    val id: String = "",
    val title: String,
    val payer: String,
    val price: String,
    val category: String = "",
    val categoryIconRes: Int = 0,
    val iconColor: Color
)

// State Khusus untuk Proses Delete
sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}

class DetailedEventViewModel : ViewModel() {
    private val repository: LucaRepository = LucaFirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    // State Tampilan Data
    private val _uiEvent = MutableStateFlow(UIEventState())
    val uiEvent = _uiEvent.asStateFlow()

    // Raw activities list
    private val _activities = MutableStateFlow<List<UIActivityState>>(emptyList())

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Combined flow for filtered activities (maintaining timestamp order from repository)
    val uiActivities = combine(_activities, _searchQuery) { activities, query ->
        if (query.isEmpty()) {
            // Case 1: Empty query - Return activities as-is (already sorted by createdAt DESC from repository)
            activities
        } else {
            // Case 2: Query is not empty - Filter activities containing the query
            // Maintain the timestamp order (newest first)
            activities.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }
    }

    // State Status Delete
    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState = _deleteState.asStateFlow()

    // State Status Delete Activity
    private val _deleteActivityState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteActivityState = _deleteActivityState.asStateFlow()

    // Logic Load Data
    fun loadEventData(eventId: String) {
        viewModelScope.launch {
            val eventRaw = repository.getEventById(eventId)

            if (eventRaw != null) {
                _uiEvent.value = UIEventState(
                    title = eventRaw.title,
                    location = eventRaw.location,
                    date = eventRaw.date,
                    imageUrl = eventRaw.imageUrl,
                    participantAvatars = eventRaw.participants.map { it.avatarName }
                )
            }

            val activitiesRaw = repository.getActivitiesByEventId(eventId)
            _activities.value = activitiesRaw.map { act ->
                UIActivityState(
                    id = act.id,
                    title = act.title,
                    payer = act.payerName,
                    price = act.amount,
                    category = act.category,
                    categoryIconRes = getCategoryIconRes(act.category),
                    iconColor = try {
                        Color(android.graphics.Color.parseColor(act.categoryColorHex))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                )
            }
        }
    }

    // Update search query
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Logic Delete Event
    fun deleteEvent(eventId: String) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            _deleteState.value = DeleteState.Error("User session not found. Please relogin.")
            return
        }

        _deleteState.value = DeleteState.Loading

        // Langsung hapus tanpa re-autentikasi password
        viewModelScope.launch {
            try {
                val result = repository.deleteEvent(eventId)
                if (result.isSuccess) {
                    _deleteState.value = DeleteState.Success
                } else {
                    // Ambil pesan error dari result jika ada, atau gunakan default
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to delete event from database."
                    _deleteState.value = DeleteState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }

    // Logic Delete Activity
    fun deleteActivity(eventId: String, activityId: String) {
        _deleteActivityState.value = DeleteState.Loading

        viewModelScope.launch {
            try {
                val result = repository.deleteActivity(eventId, activityId)
                if (result.isSuccess) {
                    _deleteActivityState.value = DeleteState.Success
                    // Reload activities after successful deletion
                    loadEventData(eventId)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Failed to delete activity."
                    _deleteActivityState.value = DeleteState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _deleteActivityState.value = DeleteState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun resetDeleteActivityState() {
        _deleteActivityState.value = DeleteState.Idle
    }

    private fun getCategoryIconRes(categoryName: String): Int {
        return when (categoryName) {
            "Food" -> R.drawable.ic_food_outline
            "Shopping" -> R.drawable.ic_cart_outline
            "Transportation" -> R.drawable.ic_car_outline
            "Entertainment" -> R.drawable.ic_ticket_outline
            "Others" -> R.drawable.ic_other_outline
            else -> R.drawable.ic_other_outline
        }
    }
}