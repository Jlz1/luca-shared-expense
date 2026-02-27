package com.luca.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luca.shared.data.ContactRepository
import com.luca.shared.model.BankAccountData
import com.luca.shared.model.Contact
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

    /**
     * Generate unique contact name by adding number suffix if name already exists
     * Example: "john" -> "john2", "john2" -> "john3", etc.
     */
    private fun generateUniqueContactName(baseName: String, existingContacts: List<Contact>): String {
        val existingNames = existingContacts.map { it.name.lowercase() }

        // Jika nama belum ada, gunakan nama asli
        if (!existingNames.contains(baseName.lowercase())) {
            return baseName
        }

        // Cari angka tertinggi dengan prefix yang sama
        var counter = 2
        var newName: String

        while (true) {
            newName = "$baseName$counter"
            if (!existingNames.contains(newName.lowercase())) {
                return newName
            }
            counter++
        }
    }

    fun addContact(
        name: String,
        phone: String,
        bankAccounts: List<BankAccountData>,
        avatarName: String = "avatar_1"
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // Generate unique name if duplicate exists
            val uniqueName = generateUniqueContactName(name, _contacts.value)

            val newContact = Contact(
                name = uniqueName,
                phoneNumber = phone,
                description = "", // Always use empty description
                bankAccounts = bankAccounts,
                avatarName = avatarName
            )

            val result = repository.addContact(newContact)
            if (result.isSuccess) {
                loadContacts()
            }
            _isLoading.value = false
        }
    }

    fun updateContact(
        contactId: String,
        name: String,
        phone: String,
        description: String,
        bankAccounts: List<BankAccountData>,
        avatarName: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // Filter out current contact untuk cek duplikat hanya dengan contact lain
            val otherContacts = _contacts.value.filter { it.id != contactId }

            // Generate unique name if duplicate exists (excluding current contact)
            val uniqueName = generateUniqueContactName(name, otherContacts)

            val updatedContact = Contact(
                id = contactId,
                name = uniqueName,
                phoneNumber = phone,
                description = description,
                bankAccounts = bankAccounts,
                avatarName = avatarName
            )

            val result = repository.updateContact(contactId, updatedContact)
            if (result.isSuccess) {
                loadContacts()
            }
            _isLoading.value = false
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteContact(contactId)
            if (result.isSuccess) {
                loadContacts()
            }
            _isLoading.value = false
        }
    }
}