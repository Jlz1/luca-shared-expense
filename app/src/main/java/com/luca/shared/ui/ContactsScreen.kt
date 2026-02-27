package com.luca.shared.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luca.shared.model.Contact
import com.luca.shared.ui.theme.*
import com.luca.shared.util.AvatarUtils
import com.luca.shared.viewmodel.ContactsViewModel
import kotlinx.coroutines.launch
import com.luca.shared.R
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.luca.shared.ui.components.UserProfileOverlay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = viewModel(),
    onMenuClick: () -> Unit = {}
) {
    val realContacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showUserProfileOverlay by remember { mutableStateOf(false) }
    var showContactDetail by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showEditContact by remember { mutableStateOf(false) }

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

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            val firstMatchIndex = getScrollIndex(groupedContacts, searchQuery.first().uppercaseChar())
            if (firstMatchIndex != -1) {
                listState.animateScrollToItem(firstMatchIndex)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UIBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(onLeftIconClick = onMenuClick)

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text("Contacts", style = AppFont.SemiBold, fontSize = 20.sp, color = UIBlack)
                Spacer(modifier = Modifier.height(16.dp))

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
                                    onClick = {
                                        selectedContact = contact
                                        showContactDetail = true
                                    },
                                    onAvatarShuffled = { newAvatarName ->
                                        viewModel.updateContact(
                                            contact.id,
                                            contact.name,
                                            contact.phoneNumber,
                                            contact.description ?: "",
                                            contact.bankAccounts,
                                            newAvatarName
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

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

        // ===== OVERLAY: ADD CONTACT =====
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

        // ===== OVERLAY: CONTACT DETAIL =====
        if (showContactDetail && selectedContact != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                ContactDetailOverlay(
                    contact = selectedContact!!,
                    onClose = {
                        showContactDetail = false
                        selectedContact = null
                    },
                    onEdit = {
                        showContactDetail = false
                        showEditContact = true
                    },
                    onDelete = {
                        selectedContact?.let { contact ->
                            viewModel.deleteContact(contact.id)
                        }
                        showContactDetail = false
                        selectedContact = null
                    }
                )
            }
        }

        // ===== OVERLAY: EDIT CONTACT =====
        if (showEditContact && selectedContact != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                UserProfileOverlay(
                    onClose = {
                        showEditContact = false
                        selectedContact = null
                    },
                    editContact = selectedContact,
                    onAddContact = { _, _, _, _ -> },
                    onUpdateContact = { id, name, phone, description, banks, avatarName ->
                        viewModel.updateContact(id, name, phone, description, banks, avatarName)
                        showEditContact = false
                        selectedContact = null
                    }
                )
            }
        }
    }
}

@Composable
fun ContactRowItem(
    contact: Contact,
    onClick: () -> Unit = {},
    onAvatarShuffled: (String) -> Unit = {}
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var displayAvatarSeed by remember { mutableStateOf(contact.avatarName) }

    val handleAvatarTap: () -> Unit = {
        tapCount++
        displayAvatarSeed = "${contact.name}${tapCount}"
        onAvatarShuffled(displayAvatarSeed)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(UIGrey)
                .clickable { handleAvatarTap() }
        ) {
            val avatarUrl = remember(displayAvatarSeed) { AvatarUtils.getDiceBearUrl(displayAvatarSeed) }

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Avatar Seed: $displayAvatarSeed",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),

                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = UIBlack,
                            strokeWidth = 2.dp
                        )
                    }
                },

                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "Error",
                            modifier = Modifier.size(20.dp),
                            alpha = 0.5f
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = contact.name,
                style = AppFont.SemiBold,
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
        currentIndex += listSize + 1
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

@Composable
fun ContactDetailOverlay(
    contact: Contact,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .fillMaxHeight(0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Close",
                    tint = UIBlack,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClose() }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = UIAccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            val avatarUrl = remember(contact.avatarName) {
                AvatarUtils.getDiceBearUrl(contact.avatarName.ifEmpty { contact.name })
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(UIGrey)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = contact.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = UIBlack,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getRandomAvatarColor(contact.name)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.take(1).uppercase(),
                                style = AppFont.SemiBold,
                                color = UIWhite,
                                fontSize = 40.sp
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = contact.name,
                style = AppFont.Bold,
                fontSize = 24.sp,
                color = UIBlack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Phone Number
            if (contact.phoneNumber.isNotEmpty()) {
                DetailSection(
                    title = "Phone Number",
                    content = contact.phoneNumber
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bank Accounts
            if (contact.bankAccounts.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bank Accounts",
                        style = AppFont.SemiBold,
                        fontSize = 16.sp,
                        color = UIBlack,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    contact.bankAccounts.forEach { bankAccount ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = UIGrey),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val logoResId = remember(bankAccount.bankName) {
                                    when (bankAccount.bankName.lowercase()) {
                                        "bca" -> R.drawable.bank_logo_bca
                                        "bri" -> R.drawable.bank_logo_bri
                                        "bni" -> R.drawable.bank_logo_bni
                                        "mandiri" -> R.drawable.bank_logo_mandiri
                                        "blu" -> R.drawable.bank_logo_blu
                                        else -> R.drawable.bank_logo_bca
                                    }
                                }

                                Image(
                                    painter = painterResource(id = logoResId),
                                    contentDescription = bankAccount.bankName,
                                    modifier = Modifier.size(40.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bankAccount.bankName,
                                        style = AppFont.SemiBold,
                                        fontSize = 16.sp,
                                        color = UIBlack
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bankAccount.accountNumber,
                                        style = AppFont.Regular,
                                        fontSize = 14.sp,
                                        color = UIDarkGrey
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = UIGrey),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No bank accounts added",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIDarkGrey
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = UIWhite,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE53935).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete Contact?",
                    style = AppFont.Bold,
                    fontSize = 20.sp,
                    color = UIBlack
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${contact.name}? This action cannot be undone.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = UIWhite, style = AppFont.SemiBold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = UIBlack, style = AppFont.Medium)
                }
            }
        )
    }
}

@Composable
fun DetailSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = AppFont.SemiBold,
            fontSize = 14.sp,
            color = UIDarkGrey,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = UIGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = content,
                style = AppFont.Medium,
                fontSize = 16.sp,
                color = UIBlack,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

