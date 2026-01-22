package com.example.luca.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- IMPORTS PENTING UNTUK VIEWMODEL ---
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.viewmodel.ContactsViewModel
// --- IMPORT MODEL DAN COMPONENTS ---
import com.example.luca.model.Contact
import com.example.luca.ui.components.* import com.example.luca.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(
    // PERBAIKAN 1: Deklarasikan ViewModel di sini agar tidak error "Unresolved reference"
    viewModel: ContactsViewModel = viewModel()
) {
    // 2. DATA DARI FIREBASE (Ganti Dummy Data)
    val realContacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State UI
    var showUserProfileOverlay by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) } // Detail Overlay

    // State Edit & Delete (Placeholder untuk logic masa depan)
    var showEditOverlay by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Logic Filter Pencarian
    val filteredContacts = remember(realContacts, searchQuery) {
        if (searchQuery.isEmpty()) {
            realContacts
        } else {
            realContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Logic Grouping (#, A, B, C...)
    val groupedContacts = remember(filteredContacts) {
        filteredContacts.sortedBy { it.name }.groupBy {
            val firstChar = it.name.firstOrNull()?.uppercaseChar() ?: '#'
            if (firstChar.isLetter()) firstChar else '#'
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val alphabet = remember { listOf('#') + ('A'..'Z').toList() }

    // Auto scroll saat search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            val firstMatchIndex = getScrollIndex(groupedContacts, searchQuery.first().uppercaseChar())
            if (firstMatchIndex != -1) {
                listState.animateScrollToItem(firstMatchIndex)
            }
        }
    }

    // --- STRUKTUR UTAMA ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UIBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Component
            HeaderSection()

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text("Contacts", style = AppFont.SemiBold, fontSize = 20.sp, color = UIBlack)
                Spacer(modifier = Modifier.height(16.dp))

                // --- SEARCH BAR & ADD BUTTON ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SearchBarModify(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                            .height(50.dp),
                        placeholder = "Search contacts...",
                        initialQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        readOnly = false
                    )

                    // Add Button (Yellow Circle)
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { showUserProfileOverlay = true },
                        shape = CircleShape,
                        color = UIAccentYellow,
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = UIBlack)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- LIST KONTAK ---
                if (isLoading) {
                    // Loading State
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UIAccentYellow)
                    }
                } else if (filteredContacts.isEmpty()) {
                    // Empty State
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isEmpty())
                                "You define your world,\nstart adding contacts now!"
                            else
                                "No contacts found for \"$searchQuery\"",
                            textAlign = TextAlign.Center,
                            style = AppFont.Regular,
                            color = UIDarkGrey,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    // Contact List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        groupedContacts.forEach { (initial, contactsForInitial) ->
                            stickyHeader(key = initial) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(UIBackground)
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = initial.toString(),
                                        style = AppFont.SemiBold,
                                        fontSize = 18.sp,
                                        color = UIBlack
                                    )
                                }
                            }

                            items(contactsForInitial) { contact ->
                                ContactRowItem(
                                    contact = contact,
                                    onContactClicked = { selectedContact = it }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- ALPHABET SIDEBAR ---
        if (filteredContacts.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = 180.dp, bottom = 90.dp)
                    .width(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                alphabet.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clickable {
                                coroutineScope.launch {
                                    val index = getScrollIndex(groupedContacts, letter)
                                    if (index != -1) listState.animateScrollToItem(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(letter.toString(), style = AppFont.Regular, fontSize = 10.sp, color = UIDarkGrey)
                    }
                }
            }
        }

        // --- OVERLAY: ADD CONTACT ---
        if (showUserProfileOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {}, // Prevent click outside closing immediately
                contentAlignment = Alignment.Center
            ) {
                UserProfileOverlay(
                    onClose = { showUserProfileOverlay = false },
                    // PERBAIKAN: ViewModel sudah didefinisikan di parameter fungsi, jadi aman dipanggil
                    onAddContact = { name, phone, desc, banks ->
                        viewModel.addContact(name, phone, desc, banks)
                        showUserProfileOverlay = false
                    }
                )
            }
        }

        // --- OVERLAY: DETAIL CONTACT ---
        if (selectedContact != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { selectedContact = null },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    // Mapping Data Model -> UI Component
                    // Karena ContactCard di Components.kt masih pakai List<BankAccount> UI lokal, kita map manual
                    val uiBankAccounts = selectedContact!!.bankAccounts.map {
                        // Perhatikan: ini mapping ke class BankAccount UI di Components.kt (jika masih ada)
                        // atau kita bisa buat ContactCard menerima BankAccountData langsung nanti.
                        // Untuk sekarang kita anggap ContactCard menerima (String, String, Color)
                        BankAccount(it.bankName, it.accountNumber, Color(0xFF1f4788)) // Warna dummy bank sementara
                    }

                    ContactCard(
                        contactName = selectedContact!!.name,
                        phoneNumber = selectedContact!!.phoneNumber,
                        avatarColor = getRandomAvatarColor(selectedContact!!.name),
                        events = emptyList(), // Event belum ada di database
                        bankAccounts = uiBankAccounts,
                        onEditClicked = {
                            // TODO: Implement Edit Logic
                            showEditOverlay = true
                        },
                        onDeleteClicked = {
                            // TODO: Implement Delete Logic
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }

        // --- OVERLAY: EDIT & DELETE (Placeholder agar kode tidak error jika state true) ---
        if (showEditOverlay) {
            // Tampilkan UI Edit (bisa copy dari kode sebelumnya jika sudah fix)
            // Untuk sekarang di-reset biar gak stuck
            LaunchedEffect(Unit) { showEditOverlay = false }
        }
        if (showDeleteConfirmation) {
            // Tampilkan UI Delete
            LaunchedEffect(Unit) { showDeleteConfirmation = false }
        }
    }
}

// --- HELPER COMPONENTS KHUSUS HALAMAN INI ---

@Composable
fun ContactRowItem(
    contact: Contact,
    onContactClicked: (Contact) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClicked(contact) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Generated dari Nama
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(getRandomAvatarColor(contact.name)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.take(1).uppercase(),
                style = AppFont.SemiBold,
                color = UIWhite,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = contact.name, style = AppFont.Regular, fontSize = 16.sp, color = UIBlack)
            Text(text = contact.phoneNumber, style = AppFont.Regular, fontSize = 14.sp, color = UIDarkGrey)
        }
    }
}

// --- LOGIC HELPERS ---

// Helper Warna Random Konsisten
fun getRandomAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF66BB6A),
        Color(0xFFFFA726), Color(0xFFAB47BC), Color(0xFF5FBDAC)
    )
    return colors[kotlin.math.abs(name.hashCode()) % colors.size]
}

// Helper Scroll Index
fun getScrollIndex(groupedContacts: Map<Char, List<Contact>>, targetChar: Char): Int {
    var currentIndex = 0
    val sortedKeys = groupedContacts.keys.sorted()
    for (key in sortedKeys) {
        if (key == targetChar) return currentIndex
        val listSize = groupedContacts[key]?.size ?: 0
        currentIndex += listSize + 1 // +1 untuk header
    }
    return -1
}