package com.luca.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luca.shared.data.LucaRepository
import com.luca.shared.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: LucaRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            // Menggunakan Flow untuk Realtime Update
            repository.getEventsFlow().collect { eventList ->
                _events.value = eventList
                _isLoading.value = false
            }
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