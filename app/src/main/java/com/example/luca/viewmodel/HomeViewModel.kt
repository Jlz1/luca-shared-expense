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

    // 1. Daftar Event
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    // 2. Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 3. Loading State (Default true agar loading muncul saat pertama buka)
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadEvents()
    }

    // --- LOGIC ---

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // UPDATE: Tidak perlu pass userId, repo cari sendiri
                val eventList = repository.getAllEvents()
                _events.value = eventList
            } catch (e: Exception) {
                e.printStackTrace()
                _events.value = emptyList()
            } finally {
                // Loading selesai (sukses/gagal)
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Logic filtering (Search)
    fun getFilteredEvents(): List<Event> {
        val query = _searchQuery.value
        val currentEvents = _events.value

        return if (query.isEmpty()) {
            currentEvents
        } else {
            // Filter berdasarkan judul (Title), ignoreCase = huruf besar/kecil dianggap sama
            currentEvents.filter { it.title.contains(query, ignoreCase = true) }
        }
    }
}