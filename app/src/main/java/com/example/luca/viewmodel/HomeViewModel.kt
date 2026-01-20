package com.example.luca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.LucaRepository
import com.example.luca.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: LucaRepository) : ViewModel() {

    // State untuk menyimpan daftar event dari database
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    // State untuk search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        // Otomatis ambil data saat ViewModel dibuat
        loadEvents()
    }

    // Fungsi untuk mengambil data dari Repository (Firebase)
    fun loadEvents() {
        viewModelScope.launch {
            val eventList = repository.getAllEvents()
            _events.value = eventList
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Logic filtering berdasarkan search query
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