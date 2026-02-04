package com.example.luca.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.algorithm.SmartSplitBill
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.data.LucaRepository
import com.example.luca.model.Settlement
import com.example.luca.model.SettlementResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State untuk UI Summary Screen
 */
data class SummaryUiState(
    val isLoading: Boolean = false,
    val settlements: List<Settlement> = emptyList(),
    val totalExpense: Long = 0L,
    val errorMessage: String? = null,
    val eventTitle: String = "",
    val isSaved: Boolean = false
)

/**
 * ViewModel untuk Summary Screen.
 * Mengambil data dari Firebase, menjalankan SmartSplitBill algorithm,
 * dan menyimpan hasilnya kembali ke Firebase.
 */
class SummaryViewModel(
    private val repository: LucaRepository = LucaFirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState = _uiState.asStateFlow()

    private val gson = Gson()

    private var currentEventId: String = ""
    private var cachedSettlementResult: SettlementResult? = null

    /**
     * Load dan calculate settlements untuk sebuah event.
     *
     * Flow:
     * 1. Ambil Event data dari Firebase
     * 2. Ambil semua Activities dari event tersebut
     * 3. Untuk setiap Activity, ambil semua Items
     * 4. Jalankan SmartSplitBill algorithm
     * 5. Simpan hasil ke Firebase sebagai `settlementResultJson` di Event document
     */
    fun loadAndCalculateSettlements(eventId: String) {
        if (eventId.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Event ID is empty")
            return
        }

        currentEventId = eventId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                Log.d("SummaryViewModel", "=== START Loading Event: $eventId ===")

                // 1. Get Event data
                val event = repository.getEventById(eventId)
                if (event == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Event not found"
                    )
                    return@launch
                }

                Log.d("SummaryViewModel", "Event found: ${event.title}")

                // Build participant avatar map
                val participantAvatars = event.participants.associate {
                    it.name to it.avatarName
                }
                Log.d("SummaryViewModel", "Participants: ${participantAvatars.keys}")

                // 2. Get all activities for this event
                val activities = repository.getActivitiesByEventId(eventId)
                Log.d("SummaryViewModel", "Found ${activities.size} activities")

                if (activities.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        eventTitle = event.title,
                        settlements = emptyList(),
                        totalExpense = 0L,
                        errorMessage = "No activities found in this event"
                    )
                    return@launch
                }

                // 3. Collect all items from all activities
                val activitiesWithItems = mutableListOf<Pair<String, List<Map<String, Any>>>>()

                for (activity in activities) {
                    val items = repository.getActivityItems(eventId, activity.id)
                    val payer = activity.payerName.ifEmpty {
                        activity.paidBy?.name ?: "Unknown"
                    }

                    Log.d("SummaryViewModel", "Activity: ${activity.title}, Payer: $payer, Items: ${items.size}")

                    if (items.isNotEmpty()) {
                        activitiesWithItems.add(payer to items)
                    }
                }

                if (activitiesWithItems.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        eventTitle = event.title,
                        settlements = emptyList(),
                        totalExpense = 0L,
                        errorMessage = "No items found in any activity"
                    )
                    return@launch
                }

                // 4. Run SmartSplitBill algorithm
                Log.d("SummaryViewModel", "Running SmartSplitBill algorithm...")
                val settlementResult = SmartSplitBill.calculateFromMultipleActivities(
                    activitiesWithItems = activitiesWithItems,
                    participantAvatars = participantAvatars
                )

                Log.d("SummaryViewModel", "Calculation complete. Settlements: ${settlementResult.settlements.size}")
                for (settlement in settlementResult.settlements) {
                    Log.d("SummaryViewModel", "  ${settlement.fromName} -> ${settlement.toName}: ${settlement.amount}")
                }

                // Cache result for later saving
                cachedSettlementResult = settlementResult

                // 5. Save result to Firebase
                saveSettlementResultToFirebase(eventId, settlementResult)

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    eventTitle = event.title,
                    settlements = settlementResult.settlements,
                    totalExpense = settlementResult.totalExpense,
                    errorMessage = null
                )

                Log.d("SummaryViewModel", "=== END Loading Event ===")

            } catch (e: Exception) {
                Log.e("SummaryViewModel", "Error calculating settlements: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Save SettlementResult ke Firebase sebagai JSON string di Event document.
     */
    private suspend fun saveSettlementResultToFirebase(eventId: String, result: SettlementResult) {
        try {
            val json = gson.toJson(result)
            Log.d("SummaryViewModel", "Saving settlement JSON to Firebase...")

            val saveResult = (repository as? LucaFirebaseRepository)?.saveSettlementResult(eventId, json)

            if (saveResult?.isSuccess == true) {
                Log.d("SummaryViewModel", "✅ Settlement result saved to Firebase")
                _uiState.value = _uiState.value.copy(isSaved = true)
            } else {
                Log.e("SummaryViewModel", "❌ Failed to save settlement result")
            }
        } catch (e: Exception) {
            Log.e("SummaryViewModel", "Error saving settlement result: ${e.message}", e)
        }
    }

    /**
     * Toggle paid status untuk sebuah settlement.
     */
    fun toggleSettlementPaid(settlementId: String) {
        val currentSettlements = _uiState.value.settlements.toMutableList()
        val index = currentSettlements.indexOfFirst { it.id == settlementId }

        if (index != -1) {
            val settlement = currentSettlements[index]
            currentSettlements[index] = settlement.copy(isPaid = !settlement.isPaid)

            _uiState.value = _uiState.value.copy(settlements = currentSettlements)

            // Save updated settlements to Firebase
            viewModelScope.launch {
                val updatedResult = SettlementResult(
                    settlements = currentSettlements,
                    totalExpense = _uiState.value.totalExpense,
                    calculatedAt = System.currentTimeMillis()
                )
                saveSettlementResultToFirebase(currentEventId, updatedResult)
            }
        }
    }

    /**
     * Get settlement result as JSON string (untuk sharing atau export).
     */
    fun getSettlementResultJson(): String {
        val result = SettlementResult(
            settlements = _uiState.value.settlements,
            totalExpense = _uiState.value.totalExpense,
            calculatedAt = System.currentTimeMillis()
        )
        return gson.toJson(result)
    }

    /**
     * Refresh/recalculate settlements.
     */
    fun refresh() {
        if (currentEventId.isNotEmpty()) {
            loadAndCalculateSettlements(currentEventId)
        }
    }

    /**
     * Load existing settlement result from Firebase (jika sudah pernah dihitung).
     */
    fun loadExistingSettlements(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val existingJson = (repository as? LucaFirebaseRepository)?.getSettlementResult(eventId)

                if (!existingJson.isNullOrEmpty()) {
                    val result = gson.fromJson(existingJson, SettlementResult::class.java)

                    val event = repository.getEventById(eventId)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        eventTitle = event?.title ?: "",
                        settlements = result.settlements,
                        totalExpense = result.totalExpense,
                        isSaved = true
                    )

                    currentEventId = eventId
                    cachedSettlementResult = result
                } else {
                    // No existing result, calculate new
                    loadAndCalculateSettlements(eventId)
                }
            } catch (e: Exception) {
                Log.e("SummaryViewModel", "Error loading existing settlements: ${e.message}", e)
                // Fallback to calculation
                loadAndCalculateSettlements(eventId)
            }
        }
    }
}

