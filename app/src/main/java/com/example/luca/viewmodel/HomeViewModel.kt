package com.example.luca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.LucaRepository
import com.example.luca.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: LucaRepository) : ViewModel() {

    // --- STATE UTAMA ---

    // 1. Daftar Event (Data dari Firebase)
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    // 2. Search Query (Apa yang diketik user)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 3. Loading State (FIX untuk masalah "Oops" flickering)
    // Default 'true' agar saat aplikasi baru buka, yang muncul loading spinner dulu
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        // Otomatis ambil data saat aplikasi dibuka
        loadEvents()
    }

    // --- LOGIC ---

    fun loadEvents() {
        viewModelScope.launch {
            // Mulai Loading
            _isLoading.value = true

            try {
                // Ambil data dari Repository
                val eventList = repository.getAllEvents()
                _events.value = eventList
            } catch (e: Exception) {
                // Jika error, print ke log (bisa ditambahkan handling error UI nanti)
                e.printStackTrace()
            } finally {
                // SELESAI Loading (Wajib jalan mau sukses atau gagal)
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Logic filtering (pencarian)
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
