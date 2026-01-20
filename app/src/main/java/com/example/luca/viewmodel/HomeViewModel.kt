package com.example.luca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.LucaRepository
import com.example.luca.model.Event
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: LucaRepository) : ViewModel() {

    // --- STATE ---
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // --- FIREBASE AUTH INSTANCE ---
    // Ini adalah "satpam" yang tahu siapa user yang sedang aktif
    private val auth = FirebaseAuth.getInstance()

    init {
        loadEvents()
    }

    // --- LOGIC UTAMA (DYNAMIC) ---
    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Cek User yang sedang Login secara REAL
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // KASUS A: User Sudah Login
                // Ambil UID asli (misal: "Kz8dJ9sX...") dari Firebase Auth
                val userId = currentUser.uid

                try {
                    // Minta data ke "kamar" user tersebut
                    val eventList = repository.getAllEvents(userId)
                    _events.value = eventList
                } catch (e: Exception) {
                    e.printStackTrace()
                    _events.value = emptyList()
                }
            } else {
                // KASUS B: Belum Login / Logout
                // Data kosong karena kita tidak tahu harus ambil data siapa
                _events.value = emptyList()

                // TODO: Di real app, biasanya di sini kita trigger navigasi ke LoginScreen
                // tapi untuk HomeViewModel, cukup kosongkan data saja.
            }

            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredEvents(): List<Event> {
        val query = _searchQuery.value
        val currentEvents = _events.value

        return if (query.isEmpty()) {
            currentEvents
        } else {
            currentEvents.filter { it.title.contains(query, ignoreCase = true) }
        }
    }
}