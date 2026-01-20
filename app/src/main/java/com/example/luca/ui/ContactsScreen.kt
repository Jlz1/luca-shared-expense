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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*
import kotlinx.coroutines.launch
// Pastikan HeaderSection diimport
// import com.example.luca.ui.components.HeaderSection

// Model Data Sederhana untuk Kontak
data class Contact(
    val id: Int,
    val name: String,
    val phoneNumber: String,
    val color: Color // Warna avatar dummy
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen() {
    // 1. DUMMY DATA GENERATOR
    // Ubah list ini jadi emptyList() untuk mengetes tampilan kosong (Empty State)
    val contacts = remember { generateDummyContacts() }
    // State untuk UserProfileOverlay
    var showUserProfileOverlay by remember { mutableStateOf(false) }

    // State untuk Search Bar
    var searchQuery by remember { mutableStateOf("") }

    // State untuk menampilkan ContactCard detail
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    // State untuk Edit Contact
    var editingContact by remember { mutableStateOf<Contact?>(null) }
    var showEditOverlay by remember { mutableStateOf(false) }

    // State untuk Delete Confirmation
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Filtered contacts berdasarkan search query (case-insensitive)
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.startsWith(searchQuery, ignoreCase = true)
            }
        }
    }

    // Grouping data berdasarkan huruf awal
    val groupedContacts = remember(filteredContacts) {
        filteredContacts.sortedBy { it.name }.groupBy {
            val firstChar = it.name.first().uppercaseChar()
            if (firstChar.isLetter()) firstChar else '#'
        }
    }


    // State untuk Scrolling
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll saat search query berubah
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            val firstMatchIndex = getFirstMatchingContactIndex(groupedContacts, searchQuery)
            if (firstMatchIndex != -1) {
                listState.animateScrollToItem(firstMatchIndex)
            }
        }
    }

    // List Huruf untuk Sidebar (# + A-Z)
    val alphabet = remember { listOf('#') + ('A'..'Z').toList() }

    // --- STRUKTUR UTAMA TANPA SCAFFOLD ---
    // Gunakan Box sebagai container utama, background ambil dari theme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UIBackground)
            // Tambahkan padding status bar agar konten tidak nabrak notifikasi bar HP
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // 1. HEADER (Logo & Avatar) - Manual Placement
            // Pastikan fungsi HeaderSection() tersedia di projectmu
            HeaderSection()

            // 2. KONTEN HALAMAN (Title, Search, List)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- TITLE ---
                Text(
                    text = "Contacts",
                    style = AppFont.SemiBold, // Pastikan font style ini ada di Theme
                    fontSize = 20.sp,
                    color = UIBlack
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- SEARCH BAR & ADD BUTTON ROW ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Bar Custom
                    SearchBarModify(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                            .height(50.dp),
                        placeholder = "Search contacts...",
                        initialQuery = searchQuery,
                        onSearchQueryChange = { query ->
                            searchQuery = query
                        },
                        readOnly = false,
                        databaseLabel = "Database: Contacts"
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

                // --- CONTACT LIST / EMPTY STATE ---
                if (filteredContacts.isEmpty()) {
                    // TAMPILAN KOSONG (EMPTY STATE)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
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
                    // TAMPILAN LIST KONTAK
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f), // Ambil sisa ruang layar
                        contentPadding = PaddingValues(bottom = 100.dp) // PENTING: Padding bawah supaya list item paling bawah tidak ketutup Navbar
                    ) {
                        groupedContacts.forEach { (initial, contactsForInitial) ->
                            // Header Huruf (Sticky)
                            stickyHeader(key = initial) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(UIBackground) // Background biar teks header gak transparan pas scroll
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

                            // Item Kontak
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

        // --- ALPHABET SIDEBAR (Floating di Kanan) ---
        // Ditaruh di luar Column utama agar posisinya absolute/floating
        if (filteredContacts.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = 150.dp, bottom = 120.dp) // Sesuaikan padding biar pas di tengah vertikal
                    .width(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                alphabet.forEach { letter ->
                    Text(
                        text = letter.toString(),
                        style = AppFont.Regular,
                        fontSize = 10.sp,
                        color = UIDarkGrey,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable {
                                // LOGIKA SCROLL KE HURUF
                                coroutineScope.launch {
                                    // Cari index header huruf tersebut di list
                                    val index = getScrollIndex(groupedContacts, letter)
                                    if (index != -1) {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                    )
                }
            }
        }

        // --- USER PROFILE OVERLAY DIALOG ---
        // Tampilkan overlay ketika showUserProfileOverlay = true
        if (showUserProfileOverlay) {
            // Scrim (Background gelap semi-transparent)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {} // Close saat klik luar
            )

            // Overlay Card di tengah
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                UserProfileOverlay(
                    onClose = { showUserProfileOverlay = false },
                    onAddContact = { showUserProfileOverlay = false },

                )
            }
        }

        // --- CONTACT DETAIL CARD OVERLAY ---
        // Tampilkan ContactCard saat ada contact yang dipilih
        if (selectedContact != null) {
            // Scrim (Background gelap semi-transparent)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { selectedContact = null } // Close saat klik luar
            )

            // Overlay Card di tengah
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = UIWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Close Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(
                                onClick = { selectedContact = null },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = UIBlack,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Contact Card Component
                        ContactCard(
                            contactName = selectedContact!!.name,
                            phoneNumber = selectedContact!!.phoneNumber,
                            avatarColor = selectedContact!!.color,
                            events = listOf("Event 1", "Event 2"), // Dummy events
                            bankAccounts = listOf(
                                BankAccount("BCA", "1234567890", Color(0xFF1f4788)),
                                BankAccount("Mandiri", "0987654321", Color(0xFFFF6B35))
                            ), // Dummy bank accounts
                            onEditClicked = {
                                // Kerangka untuk Edit Contact
                                editingContact = selectedContact
                                showEditOverlay = true
                            },
                            onDeleteClicked = {
                                // Kerangka untuk Delete Contact
                                contactToDelete = selectedContact
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
            }
        }

        // --- EDIT CONTACT OVERLAY ---
        // Tampilkan overlay edit ketika Edit button diklik
        if (showEditOverlay && editingContact != null) {
            // Scrim (Background gelap semi-transparent)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showEditOverlay = false } // Close saat klik luar
            )

            // Overlay Edit Form di tengah
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = UIWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Title
                        Text(
                            text = "Edit Contact",
                            style = AppFont.Bold,
                            fontSize = 24.sp,
                            color = UIBlack
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // KERANGKA: Edit Form akan ditampilkan di sini
                        // Data dari editingContact akan ditampilkan dan bisa diedit
                        Text(
                            text = "Kerangka untuk Edit Form",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIDarkGrey
                        )

                        Text(
                            text = "Contact: ${editingContact?.name}",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIBlack,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = "Phone: ${editingContact?.phoneNumber}",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIBlack,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel Button
                            Button(
                                onClick = { showEditOverlay = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UIDarkGrey)
                            ) {
                                Text("Cancel", color = UIWhite, style = AppFont.Medium)
                            }

                            // Save Button (KERANGKA)
                            Button(
                                onClick = {
                                    // KERANGKA: Implementasi save logic untuk edit contact
                                    // - Update contact data dari editingContact
                                    // - Tutup overlay
                                    showEditOverlay = false
                                    selectedContact = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow)
                            ) {
                                Text("Save", color = UIBlack, style = AppFont.Medium)
                            }
                        }
                    }
                }
            }
        }

        // --- DELETE CONFIRMATION DIALOG ---
        // Tampilkan confirmation dialog ketika Delete button diklik
        if (showDeleteConfirmation && contactToDelete != null) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showDeleteConfirmation = false }
            )

            // Confirmation Dialog
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = UIWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Warning Icon or Title
                        Text(
                            text = "Delete Contact?",
                            style = AppFont.Bold,
                            fontSize = 20.sp,
                            color = UIBlack
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirmation Message
                        Text(
                            text = "Are you sure you want to delete ${contactToDelete?.name}?",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIDarkGrey,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel Button
                            Button(
                                onClick = {
                                    showDeleteConfirmation = false
                                    contactToDelete = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UIDarkGrey)
                            ) {
                                Text("Cancel", color = UIWhite, style = AppFont.Medium, fontSize = 14.sp)
                            }

                            // Delete Button (KERANGKA)
                            Button(
                                onClick = {
                                    // KERANGKA: Implementasi delete logic
                                    // - Hapus contact dari database/list
                                    // - Untuk sekarang tidak terjadi apa-apa
                                    showDeleteConfirmation = false
                                    selectedContact = null
                                    contactToDelete = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                            ) {
                                Text("Delete", color = UIWhite, style = AppFont.Medium, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENT: CONTACT ROW ITEM ---
@Composable
fun ContactRowItem(
    contact: Contact,
    onContactClicked: (Contact) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClicked(contact) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Bulat
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(contact.color),
            contentAlignment = Alignment.Center
        ) {
            // Tampilkan inisial nama di avatar jika tidak ada foto
            Text(
                text = contact.name.take(1), // Ambil huruf pertama
                style = AppFont.SemiBold,
                color = UIWhite,
                fontSize = 20.sp
            )
            // Note: Nanti bisa diganti Image(...) jika ada foto profil real
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Nama dan Nomor
        Column {
            Text(
                text = contact.name,
                style = AppFont.Regular,
                fontSize = 16.sp,
                color = UIBlack
            )
            Text(
                text = contact.phoneNumber,
                style = AppFont.Regular,
                fontSize = 14.sp,
                color = UIDarkGrey
            )
        }
    }
}

// --- HELPER FUNCTIONS ---

// 1. Fungsi untuk mencari posisi index scroll berdasarkan huruf header
fun getScrollIndex(groupedContacts: Map<Char, List<Contact>>, targetChar: Char): Int {
    var currentIndex = 0
    // Urutkan key agar pencarian index akurat
    val sortedKeys = groupedContacts.keys.sorted()

    for (key in sortedKeys) {
        if (key == targetChar) {
            return currentIndex
        }
        val listSize = groupedContacts[key]?.size ?: 0
        // Tambahkan jumlah item + 1 (untuk headernya sendiri)
        currentIndex += listSize + 1
    }
    return -1 // Tidak ketemu
}

// 2. Fungsi untuk mencari index kontak pertama yang cocok dengan search query
fun getFirstMatchingContactIndex(groupedContacts: Map<Char, List<Contact>>, query: String): Int {
    if (query.isEmpty()) return -1

    var currentIndex = 0
    val sortedKeys = groupedContacts.keys.sorted()

    for (key in sortedKeys) {
        // Tambah 1 untuk header
        currentIndex += 1

        val contactsForKey = groupedContacts[key] ?: continue
        for (contact in contactsForKey) {
            if (contact.name.contains(query, ignoreCase = true)) {
                return currentIndex
            }
            currentIndex += 1
        }
    }
    return -1 // Tidak ketemu
}

// 2. Generator Dummy Data
fun generateDummyContacts(): List<Contact> {
    val colors = listOf(Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFFFA726), Color(0xFFAB47BC))

    return listOf(
        Contact(1, "Aldi Faustinus", "0812 3456 7890", colors[0]),
        Contact(2, "Abel Mahotama", "0812 3456 7890", colors[3]),
        Contact(3, "Agus Tantrum", "0812 3456 7890", colors[1]),
        Contact(4, "Alex Xander", "0812 3456 7890", colors[0]),
        Contact(5, "Bagus Gus", "0812 3456 7890", colors[3]),
        Contact(6, "Beben Rafli", "0812 3456 7890", colors[4]),
        Contact(7, "Claudia Valerie", "0812 3456 7890", colors[2]),
        Contact(8, "Chandra Li", "0812 3456 7890", colors[1]),
        Contact(9, "Dadang Suradang", "0812 3456 7890", colors[4]),
        Contact(10, "Doni Salmanan", "0812 3456 7890", colors[0]),
        Contact(11, "Eka Gustiwana", "0812 3456 7890", colors[2]),
        Contact(12, "Fajar Sadboy", "0812 3456 7890", colors[3]),
        Contact(13, "Gading Marten", "0812 3456 7890", colors[1]),
        Contact(14, "Hesti Purwadinata", "0812 3456 7890", colors[4]),
        Contact(15, "Indra Bekti", "0812 3456 7890", colors[0]),
        Contact(16, "Joko Anwar", "0812 3456 7890", colors[2]),
        Contact(17, "Kaesang", "0812 3456 7890", colors[3]),
        Contact(18, "Luna Maya", "0812 3456 7890", colors[1]),
        Contact(19, "Maudy Ayunda", "0812 3456 7890", colors[0]),
        Contact(20, "Zayn Malik", "0812 3456 7890", colors[4]),
    )
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenPreview() {
    LucaTheme {
        ContactsScreen()
    }
}