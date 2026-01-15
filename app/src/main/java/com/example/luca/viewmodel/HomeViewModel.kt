package com.example.luca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.model.Event
import com.example.luca.data.LucaRepository
import com.example.luca.data.LucaFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository: LucaRepository = LucaFirebaseRepository()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = repository.getAllEvents()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredEvents(): List<Event> {
        val query = _searchQuery.value
        return if (query.isEmpty()) {
            _events.value
        } else {
            _events.value.filter { it.title.contains(query, ignoreCase = true) }
        }
    }
}