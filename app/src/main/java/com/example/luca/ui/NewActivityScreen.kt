package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.model.Contact
import com.example.luca.R
import com.example.luca.ui.components.ContactSelectionOverlay
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBackground
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite
import com.example.luca.util.AvatarUtils
import com.example.luca.viewmodel.AddEventViewModel

@Composable
fun AddActivityScreen(
    viewModel: AddEventViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    // 1. State Definition (State disimpan di sini)
    var titleInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select a Category") }
    val currentUser = remember { Contact(name = "You", avatarName = "avatar_1") }
    var selectedPayer by remember { mutableStateOf(currentUser) }

    val selectedParticipants by viewModel.selectedParticipants.collectAsState()
    var showContactSelectionOverlay by remember { mutableStateOf(false) }
    var showAddNewContactOverlay by remember { mutableStateOf(false) }

    val availableContacts by viewModel.availableContacts.collectAsState()

    if (showContactSelectionOverlay) {
        Dialog(
            onDismissRequest = { showContactSelectionOverlay = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                ContactSelectionOverlay(
                    availableContacts = availableContacts,
                    selectedContacts = selectedParticipants,
                    onDismiss = { showContactSelectionOverlay = false },
                    onConfirm = { contacts ->
                        viewModel.updateSelectedParticipants(contacts)
                        showContactSelectionOverlay = false
                    },
                    onAddNewContact = {
                        // Tutup selection overlay, buka form add new contact
                        showContactSelectionOverlay = false
                        showAddNewContactOverlay = true
                    }
                )
            }
        }
    }

    if (showAddNewContactOverlay) {
        Dialog(
            onDismissRequest = { showAddNewContactOverlay = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                UserProfileOverlay(
                    onClose = {
                        showAddNewContactOverlay = false
                        showContactSelectionOverlay = true
                    },
                    // FIX ERROR: Parameter diurutkan sesuai UserProfileOverlay (Name, Phone, Banks, Avatar)
                    onAddContact = { name, phone, banks, avatarName ->
                        // Panggil ViewModel (Name, Phone, Avatar, Banks)
                        viewModel.addNewContact(name, phone, avatarName, banks)

                        showAddNewContactOverlay = false
                        showContactSelectionOverlay = true
                    }
                )
            }
        }
    }

    // 2. Call Content
    AddActivityScreenContent(
        selectedParticipants = selectedParticipants,
        titleInput = titleInput,
        selectedCategory = selectedCategory,
        selectedPayer = selectedPayer,
        onTitleChange = { titleInput = it },
        onCategoryClick = { /* Logic open category sheet/dialog */ },
        onPayerChange = { contact ->
            selectedPayer = contact
        },
        onPayerClick = { /* Logic open payer sheet/dialog */ },
        onBackClick = onBackClick,
        onContinueClick = onContinueClick,
        onAddParticipantClick = {  showContactSelectionOverlay = true  }
    )
}

data class CategoryOption(val name: String, val iconRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreenContent(
    // Data Parameter
    selectedParticipants: List<Contact>,
    titleInput: String,
    selectedCategory: String,
    selectedPayer: Contact,
    // Event Parameter
    onTitleChange: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onPayerChange: (Contact) -> Unit,
    onPayerClick: () -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onAddParticipantClick: () -> Unit
) {
    val currentUser = remember { Contact(name = "You", avatarName = "avatar_1") }
    val payerOptions = remember(selectedParticipants) {
        listOf(currentUser) + selectedParticipants.filter { it.name != "You" }
    }
    var isPayerExpanded by remember { mutableStateOf(false) }

    // 1. Definisikan List Kategori
    val categoryOptions = remember {
        listOf(
            CategoryOption("Food", R.drawable.ic_food_outline),
            CategoryOption("Shopping", R.drawable.ic_cart_outline),
            CategoryOption("Transportation", R.drawable.ic_car_outline),
            CategoryOption("Entertainment", R.drawable.ic_ticket_outline),
            CategoryOption("Others", R.drawable.ic_other_outline)
        )
    }

// 2. State lokal untuk dropdown
    var isCategoryExpanded by remember { mutableStateOf(false) }

// 3. Cari object kategori berdasarkan nama yang sedang terpilih (selectedCategory String)
    val currentCategoryIcon = categoryOptions.find { it.name == selectedCategory }?.iconRes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {

        // 2. HEADER
        HeaderSection(
            currentState = HeaderState.NEW_ACTIVITY,
            onLeftIconClick = onBackClick
        )

        // 3. KONTEN AREA (PUTIH & ROUNDED)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(UIBackground)
        ) {

            // A. SCROLLABLE CONTENT (FORM)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                InputSection(
                    label = "Title",
                    value = titleInput,
                    placeholder = "New Activity",
                    testTag = "input_activity_title",
                    onValueChange = onTitleChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Participants",
                    style = AppFont.SemiBold,
                    fontSize = 16.sp,
                    color = UIBlack,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    ParticipantAvatarItem(isAddButton = true, onClick = onAddParticipantClick)

                    selectedParticipants.forEach { contact ->
                        ParticipantAvatarItem(
                            name = contact.name,
                            avatarName = contact.avatarName,
                            onClick = { /* Logic remove user can be added via callback later */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    DropdownTriggerSection(
                        label = "Category",
                        displayText = selectedCategory,
                        leadingIcon = {
                            // Logic: Tampilkan Icon Kategori jika ada, jika tidak (default) tampilkan titik tiga
                            val iconRes = currentCategoryIcon ?: Icons.Default.MoreVert

                            // Container Lingkaran Abu-abu
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(UIGrey), // Background lingkaran
                                contentAlignment = Alignment.Center
                            ) {
                                // Render Icon (bisa vector standard atau drawable resource)
                                if (currentCategoryIcon != null) {
                                    Icon(
                                        painter = painterResource(id = iconRes as Int),
                                        contentDescription = "Category Icon",
                                        tint = UIBlack,
                                        modifier = Modifier.size(24.dp) // Ukuran icon di dalam lingkaran
                                    )
                                } else {
                                    Icon(
                                        imageVector = iconRes as androidx.compose.ui.graphics.vector.ImageVector,
                                        contentDescription = "Default Icon",
                                        tint = UIBlack
                                    )
                                }
                            }
                        },
                        onClick = { isCategoryExpanded = true }
                    )

                    // MENU DROPDOWN
                    DropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false },
                        modifier = Modifier
                            .background(UIWhite)
                            .width(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        categoryOptions.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Icon Kategori di dalam Menu
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp) // Sedikit lebih kecil di menu
                                                .clip(CircleShape)
                                                .background(UIGrey),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = category.iconRes),
                                                contentDescription = null,
                                                tint = UIBlack,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Text(
                                            text = category.name,
                                            style = AppFont.Regular,
                                            fontSize = 14.sp,
                                            color = UIBlack
                                        )
                                    }
                                },
                                onClick = {
                                    onCategoryClick() // Ini trigger logic lama

                                    isCategoryExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    DropdownTriggerSection(
                        label = "Paid by",
                        displayText = selectedPayer.name,
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(4.dp), // Sedikit padding biar ga mepet border
                                contentAlignment = Alignment.Center
                            ) {
                                // GUNAKAN SNIPPET KAMU DI SINI UNTUK TAMPILAN TERPILIH
                                Image(
                                    painter = painterResource(id = AvatarUtils.getAvatarResId(selectedPayer.avatarName)),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        },
                        onClick = { isPayerExpanded = true }
                    )

                    // CUSTOM DROPDOWN MENU
                    DropdownMenu(
                        expanded = isPayerExpanded,
                        onDismissRequest = { isPayerExpanded = false },
                        modifier = Modifier
                            .background(UIWhite)
                            .width(240.dp) // Lebih lebar sedikit biar nama panjang muat
                            .clip(RoundedCornerShape(12.dp)) // Bikin menu rounded
                    ) {
                        payerOptions.forEach { contact ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // TAMPILKAN AVATAR JUGA DI DALAM LIST PILIHAN
                                        Box(modifier = Modifier.size(32.dp)) {
                                            Image(
                                                painter = painterResource(id = AvatarUtils.getAvatarResId(contact.avatarName)),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }

                                        Text(
                                            text = contact.name,
                                            style = AppFont.Regular,
                                            fontSize = 14.sp,
                                            color = UIBlack
                                        )
                                    }
                                },
                                onClick = {
                                    onPayerChange(contact)
                                    isPayerExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Spacer PENTING agar tidak tertutup tombol sticky
                Spacer(modifier = Modifier.height(100.dp))
            }

            // B. STICKY BOTTOM BUTTON
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(UIBackground)
                    .padding(20.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "Continue",
                    onClick = onContinueClick
                )
            }
        }
    }
}

// Komponen helper lokal untuk dropdown (tetap di sini atau bisa dipindah ke Components jika mau)
@Composable
fun DropdownTriggerSection(
    label: String,
    displayText: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = AppFont.SemiBold,
            fontSize = 16.sp,
            color = UIBlack,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() },
            color = UIWhite,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = displayText,
                        style = AppFont.Regular,
                        color = UIDarkGrey,
                        fontSize = 16.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select",
                    tint = UIBlack
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Add Activity Content Preview"
)
@Composable
fun AddActivityScreenContentPreview() {
    // 1. Setup Dummy Data
    val dummyContacts = listOf(
        Contact(name = "", avatarName = "")
    )

    MaterialTheme {
        AddActivityScreenContent(
            selectedParticipants = dummyContacts,
            titleInput = "Makan Siang Bareng",
            selectedCategory = "Food & Drink",
            selectedPayer = Contact(name = "You", avatarName = "avatar_1"),
            onTitleChange = {},
            onCategoryClick = {},
            onPayerClick = {},
            onBackClick = {},
            onContinueClick = {},
            onAddParticipantClick = {},
            onPayerChange = {}
        )
    }
}