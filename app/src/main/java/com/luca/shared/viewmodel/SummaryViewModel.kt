package com.luca.shared.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luca.shared.algorithm.SmartSplitBill
import com.luca.shared.data.LucaFirebaseRepository
import com.luca.shared.data.LucaRepository
import com.luca.shared.model.Settlement
import com.luca.shared.model.SettlementResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class untuk expense item detail (untuk Details tab)
 */
data class ExpenseItemDetail(
    val activityTitle: String = "",
    val itemName: String = "",
    val price: Long = 0L,
    val quantity: Int = 1,
    val splitAmount: Long = 0L // Amount this user needs to pay for this item
)

/**
 * Data class untuk user consumption detail (untuk Details tab)
 */
data class UserConsumptionDetail(
    val userName: String = "",
    val avatarName: String = "",
    val totalConsumption: Long = 0L,
    val items: List<ExpenseItemDetail> = emptyList()
)

/**
 * State untuk UI Summary Screen
 */
data class SummaryUiState(
    val isLoading: Boolean = false,
    val settlements: List<Settlement> = emptyList(),
    val totalExpense: Long = 0L,
    val errorMessage: String? = null,
    val eventTitle: String = "",
    val isSaved: Boolean = false,
    val consumptionDetails: List<UserConsumptionDetail> = emptyList()
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

                // 5. Calculate consumption details (who consumed what)
                val consumptionDetails = calculateConsumptionDetails(activities, participantAvatars, eventId)

                // 6. Save result to Firebase
                saveSettlementResultToFirebase(eventId, settlementResult)

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    eventTitle = event.title,
                    settlements = settlementResult.settlements,
                    totalExpense = settlementResult.totalExpense,
                    errorMessage = null,
                    consumptionDetails = consumptionDetails
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
     * Calculate consumption details for each user.
     * Groups items by WHO CONSUMED them (memberNames), not who paid.
     */
    private suspend fun calculateConsumptionDetails(
        activities: List<com.luca.shared.model.Activity>,
        participantAvatars: Map<String, String>,
        eventId: String
    ): List<UserConsumptionDetail> {
        // Map to store user -> list of expense items
        val userConsumptionMap = mutableMapOf<String, MutableList<ExpenseItemDetail>>()

        // Iterate through all activities and their items
        for (activity in activities) {
            val items = repository.getActivityItems(eventId, activity.id)

            for (itemMap in items) {
                // Convert map to Item object
                val itemName = itemMap["itemName"] as? String ?: "Unknown Item"
                val price = (itemMap["price"] as? Number)?.toLong() ?: 0L
                val quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 1
                val memberNames = (itemMap["memberNames"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                // Calculate split amount per person
                val totalItemCost = price * quantity
                val splitAmount = if (memberNames.isNotEmpty()) {
                    totalItemCost / memberNames.size
                } else {
                    0L
                }

                // Create expense item detail
                val expenseItem = ExpenseItemDetail(
                    activityTitle = activity.title,
                    itemName = itemName,
                    price = price,
                    quantity = quantity,
                    splitAmount = splitAmount
                )

                // Add to each member who consumed this item
                for (memberName in memberNames) {
                    if (!userConsumptionMap.containsKey(memberName)) {
                        userConsumptionMap[memberName] = mutableListOf()
                    }
                    userConsumptionMap[memberName]?.add(expenseItem)
                }
            }
        }

        // Convert map to list of UserConsumptionDetail
        val consumptionDetails = userConsumptionMap.map { (userName, items) ->
            val totalConsumption = items.sumOf { it.splitAmount }
            val avatarName = participantAvatars[userName] ?: "avatar_1"

            UserConsumptionDetail(
                userName = userName,
                avatarName = avatarName,
                totalConsumption = totalConsumption,
                items = items.sortedByDescending { it.splitAmount } // Sort by amount, highest first
            )
        }.sortedByDescending { it.totalConsumption } // Sort users by total consumption

        Log.d("SummaryViewModel", "Consumption details calculated for ${consumptionDetails.size} users")

        return consumptionDetails
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

