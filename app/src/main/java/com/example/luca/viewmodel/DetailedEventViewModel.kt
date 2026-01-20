package com.example.luca.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.R
import com.example.luca.data.LucaRepository
import com.example.luca.data.LucaFirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Wrapper State untuk UI
data class UIEventState(
    val title: String = "",
    val location: String = "",
    val date: String = "",
    // UPDATE: Kita pakai String URL biar bisa load image asli dari Firebase Storage
    // Nanti di UI pakai AsyncImage (Coil)
    val imageUrl: String = "",
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
    // TAMBAHAN: Butuh Auth untuk ambil ID User
    private val auth = FirebaseAuth.getInstance()

    private val _uiEvent = MutableStateFlow(UIEventState())
    val uiEvent = _uiEvent.asStateFlow()

    private val _uiActivities = MutableStateFlow<List<UIActivityState>>(emptyList())
    val uiActivities = _uiActivities.asStateFlow()

    fun loadEventData(eventId: String) {
        viewModelScope.launch {
            // 1. Ambil User ID yang sedang login
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid

                // 2. FIX: Masukkan userId ke fungsi getEventById
                val eventRaw = repository.getEventById(userId, eventId)

                if (eventRaw != null) {
                    // LOGIC MAPPING: Data Mentah -> Visual
                    _uiEvent.value = UIEventState(
                        title = eventRaw.title,
                        location = eventRaw.location,
                        date = eventRaw.date,
                        // Ambil URL asli dari database
                        imageUrl = eventRaw.imageUrl,
                        // Contoh Logic Warna: Hardcode sementara
                        participantColors = listOf(Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFFFFD54F))
                    )
                }

                // Note: getActivitiesByEventId sementara masih pakai logic lama (root collection)
                // Kalau nanti activities dipindah ke sub-collection user, ini perlu diubah juga.
                val activitiesRaw = repository.getActivitiesByEventId(eventId)

                _uiActivities.value = activitiesRaw.map { act ->
                    UIActivityState(
                        title = act.title,
                        payer = act.payerName,
                        price = act.amount,
                        iconColor = try {
                            Color(android.graphics.Color.parseColor(act.categoryColorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    )
                }
            }
        }
    }
}