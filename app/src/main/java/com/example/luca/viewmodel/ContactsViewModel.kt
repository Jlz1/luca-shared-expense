package com.example.luca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luca.data.ContactRepository
import com.example.luca.model.BankAccountData
import com.example.luca.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {
    private val repository = ContactRepository()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getContacts()
            _contacts.value = result.sortedBy { it.name }
            _isLoading.value = false
        }
    }

    fun addContact(
        name: String,
        phone: String,
        description: String,
        bankAccounts: List<BankAccountData>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val newContact = Contact(
                name = name,
                phoneNumber = phone,
                description = description,
                bankAccounts = bankAccounts,
                avatarName = "default" // Bisa dikembangkan logic warnanya nanti di UI
            )

            val result = repository.addContact(newContact)
            if (result.isSuccess) {
                loadContacts()
            }
            _isLoading.value = false
        }
    }
}