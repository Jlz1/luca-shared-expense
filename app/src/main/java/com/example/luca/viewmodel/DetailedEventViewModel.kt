package com.example.luca.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.R
// Perhatikan import ini sudah mengarah ke folder 'data'
import com.example.luca.data.LucaRepository
import com.example.luca.data.LucaFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Wrapper State untuk UI (Visual Data)
data class UIEventState(
    val title: String = "",
    val location: String = "",
    val date: String = "",
    val imageRes: Int? = null,
    val participantColors: List<Color> = emptyList()
)

data class UIActivityState(
    val title: String,
    val payer: String,
    val price: String,
    val iconColor: Color
)

class DetailedEventViewModel : ViewModel() {
    private val repository: LucaRepository = LucaFirebaseRepository()

    private val _uiEvent = MutableStateFlow(UIEventState())
    val uiEvent = _uiEvent.asStateFlow()

    private val _uiActivities = MutableStateFlow<List<UIActivityState>>(emptyList())
    val uiActivities = _uiActivities.asStateFlow()

    fun loadEventData(eventId: String) {
        viewModelScope.launch {
            val eventRaw = repository.getEventById(eventId)

            if (eventRaw != null) {
                // LOGIC MAPPING: Data Mentah -> Visual
                _uiEvent.value = UIEventState(
                    title = eventRaw.title,
                    location = eventRaw.location,
                    date = eventRaw.date,
                    // Contoh Logic Gambar: Kalau di DB tulisannya "img_bali", load resource Bali
                    imageRes = if (eventRaw.imageUrl.contains("bali", true)) R.drawable.bg_accent_final_page else null,
                    // Contoh Logic Warna: Hardcode sementara atau ambil list Hex dari DB
                    participantColors = listOf(Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFFFFD54F))
                )
            }

            val activitiesRaw = repository.getActivitiesByEventId(eventId)
            _uiActivities.value = activitiesRaw.map { act ->
                UIActivityState(
                    title = act.title,
                    payer = act.payerName,
                    price = act.amount,
                    // Convert Hex String (#FF0000) jadi Color Object
                    iconColor = try { Color(android.graphics.Color.parseColor(act.categoryColorHex)) } catch (e: Exception) { Color.Gray }
                )
            }
        }
    }
}