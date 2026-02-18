package com.example.luca.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.model.Contact
import com.example.luca.ui.theme.*
import com.example.luca.util.AvatarUtils
import com.example.luca.viewmodel.ContactsViewModel
import kotlinx.coroutines.launch
import com.example.luca.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = viewModel(),
    onMenuClick: () -> Unit = {}
) {
    // 1. Collect Data
    val realContacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 2. UI States
    var showUserProfileOverlay by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) } // Untuk Detail Overlay
    var editingContact by remember { mutableStateOf<Contact?>(null) } // Untuk Edit Overlay
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // 3. Logic Filter & Grouping
    val filteredContacts = remember(realContacts, searchQuery) {
        if (searchQuery.isEmpty()) {
            realContacts
        } else {
            realContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val groupedContacts = remember(filteredContacts) {
        filteredContacts.sortedBy { it.name }.groupBy {
            val firstChar = it.name.firstOrNull()?.uppercaseChar() ?: '#'
            if (firstChar.isLetter()) firstChar else '#'
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val alphabet = remember { listOf('#') + ('A'..'Z').toList() }

    // 4. Auto scroll saat search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            val firstMatchIndex = getScrollIndex(groupedContacts, searchQuery.first().uppercaseChar())
            if (firstMatchIndex != -1) {
                listState.animateScrollToItem(firstMatchIndex)
            }
        }
    }

    // --- MAIN UI STRUCTURE ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UIBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            HeaderSection(onLeftIconClick = onMenuClick)

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text("Contacts", style = AppFont.SemiBold, fontSize = 20.sp, color = UIBlack)
                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar & Add Button
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

                // List Content
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UIAccentYellow)
                    }
                } else if (filteredContacts.isEmpty()) {
                    NotFoundMessage(
                        searchQuery = searchQuery,
                        emptyStateMessage = "You define your world,\nstart adding contacts now!",
                        notFoundMessage = "No contacts found for \"$searchQuery\""
                    )
                } else {
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

        // Alphabet Sidebar
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

        // Overlay: Add Contact
        if (showUserProfileOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                UserProfileOverlay(
                    onClose = { showUserProfileOverlay = false },
                    onAddContact = { name, phone, banks, avatarName ->
                        viewModel.addContact(name, phone, banks, avatarName)
                        showUserProfileOverlay = false
                    }
                )
            }
        }

        // Overlay: Detail Contact
        if (selectedContact != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { selectedContact = null },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    val uiBankAccounts = selectedContact!!.bankAccounts.map {
                        BankAccount(
                            bankName = it.bankName,
                            accountNumber = it.accountNumber,
                            bankColor = Color(0xFF1f4788)
                        )
                    }

                    ContactCard(
                        contactName = selectedContact!!.name,
                        phoneNumber = selectedContact!!.phoneNumber,
                        avatarName = selectedContact!!.avatarName,
                        avatarColor = getRandomAvatarColor(selectedContact!!.name),
                        events = emptyList(),
                        bankAccounts = uiBankAccounts,
                        onEditClicked = {
                            editingContact = selectedContact
                            selectedContact = null
                        },
                        onDeleteClicked = {
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }

        // Overlay: Delete Confirmation
        if (showDeleteConfirmation && selectedContact != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showDeleteConfirmation = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = UIWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hapus Contact?",
                            style = AppFont.Bold,
                            fontSize = 20.sp,
                            color = UIBlack
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Apakah Anda yakin ingin menghapus contact '${selectedContact!!.name}'?",
                            style = AppFont.Regular,
                            fontSize = 16.sp,
                            color = UIDarkGrey,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showDeleteConfirmation = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Batal", color = UIBlack, style = AppFont.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (selectedContact!!.id.isNotEmpty()) {
                                        viewModel.deleteContact(selectedContact!!.id)
                                    }
                                    showDeleteConfirmation = false
                                    selectedContact = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Hapus", color = UIWhite, style = AppFont.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Overlay: Edit Contact
        if (editingContact != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                UserProfileOverlay(
                    onClose = { editingContact = null },
                    onAddContact = { _, _, _, _ -> },
                    editContact = editingContact,
                    onUpdateContact = { contactId, name, phone, desc, banks, avatarName ->
                        viewModel.updateContact(contactId, name, phone, desc, banks, avatarName)
                        editingContact = null
                    }
                )
            }
        }
    }
}

// --- HELPER FUNCTIONS YANG TADI HILANG ---

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
        // Avatar Display (DiceBear atau Letter)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(UIGrey), // Background sementara
            contentAlignment = Alignment.Center
        ) {
            if (contact.avatarName.isNotEmpty()) {
                // --- KODE BARU (DICEBEAR) ---
                val avatarUrl = remember(contact.avatarName) {
                    "https://api.dicebear.com/9.x/avataaars/png?seed=${contact.avatarName}"
                }

                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Contact Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(R.drawable.ic_user_form),
                    error = painterResource(R.drawable.ic_user_form)              )
            } else {
                // Fallback ke inisial huruf
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(getRandomAvatarColor(contact.name)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        style = AppFont.SemiBold, // Pastikan AppFont terimport
                        color = UIWhite,
                        fontSize = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = contact.name,
                style = AppFont.SemiBold, // Sedikit ditebalkan biar lebih jelas
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

fun getRandomAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF66BB6A),
        Color(0xFFFFA726), Color(0xFFAB47BC), Color(0xFF5FBDAC)
    )
    return colors[kotlin.math.abs(name.hashCode()) % colors.size]
}