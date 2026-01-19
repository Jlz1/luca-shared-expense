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
import androidx.compose.material.icons.filled.Search
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
import kotlin.collections.iterator
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
fun ContactsScreen(
    onHomeClick: () -> Unit = {} // Parameter sisa, biarkan saja agar kompatibel
) {
    // 1. DUMMY DATA GENERATOR
    // Ubah list ini jadi emptyList() untuk mengetes tampilan kosong (Empty State)
    val contacts = remember { generateDummyContacts() }

    // Grouping data berdasarkan huruf awal
    val groupedContacts = remember(contacts) {
        contacts.sortedBy { it.name }.groupBy {
            val firstChar = it.name.first().uppercaseChar()
            if (firstChar.isLetter()) firstChar else '#'
        }
    }

    // State untuk Search Bar
    var searchQuery by remember { mutableStateOf("") }

    // State untuk Scrolling
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search Bar Custom
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        color = UIWhite,
                        shadowElevation = 2.dp
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text("Search", style = AppFont.Regular, color = UIDarkGrey)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = UIBlack)
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Add Button (Yellow Circle)
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { /* Handle Add Contact logic here */ },
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
                if (contacts.isEmpty()) {
                    // TAMPILAN KOSONG (EMPTY STATE)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You define your world,\nstart adding contacts now!",
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
                                ContactRowItem(contact = contact)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- ALPHABET SIDEBAR (Floating di Kanan) ---
        // Ditaruh di luar Column utama agar posisinya absolute/floating
        if (contacts.isNotEmpty()) {
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
    }
}

// --- SUB-COMPONENT: CONTACT ROW ITEM ---
@Composable
fun ContactRowItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle Click Contact */ },
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