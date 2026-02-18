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
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.luca.viewmodel.AddActivityViewModel
import com.example.luca.model.Event
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun AddActivityScreen(
    eventId: String,
    event: Event,
    viewModel: AddActivityViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onDoneClick: (eventId: String, activityId: String) -> Unit = { _, _ -> }
) {
    // Initialize ViewModel dengan event data
    LaunchedEffect(eventId) {
        viewModel.setEventId(eventId)
        viewModel.loadEventParticipants(event)
    }

    // 1. State Definition (State disimpan di sini)
    var titleInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedPayer by remember { mutableStateOf<Contact?>(null) }
    var showContactSelectionOverlay by remember { mutableStateOf(false) }

    val selectedParticipants by viewModel.selectedParticipants.collectAsState()
    val eventParticipants by viewModel.eventParticipants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val createdActivityId by viewModel.createdActivityId.collectAsState()

    // Guard untuk mencegah klik back berkali-kali yang menyebabkan bug navigation
    val backClicked = remember { mutableStateOf(false) }
    val handleBackClick: () -> Unit = {
        if (!backClicked.value) {
            backClicked.value = true
            onBackClick()
        }
    }

    // Handle success - LANGSUNG navigate saat isSuccess berubah jadi true
    LaunchedEffect(isSuccess) {
        if (isSuccess && createdActivityId.isNotEmpty()) {
            android.util.Log.d("AddActivityScreen", "SUCCESS! Navigating to activity: $createdActivityId")
            onDoneClick(eventId, createdActivityId)
        }
    }

    if (showContactSelectionOverlay) {
        Dialog(
            onDismissRequest = { showContactSelectionOverlay = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                ContactSelectionOverlay(
                    currentUser = Contact(name = "You", avatarName = "avatar_1"),
                    availableContacts = eventParticipants,
                    selectedContacts = selectedParticipants,
                    onDismiss = { showContactSelectionOverlay = false },
                    onConfirm = { contacts ->
                        viewModel.updateSelectedParticipants(contacts)
                        showContactSelectionOverlay = false
                    },
                    onAddNewContact = {} // Disable add new contact untuk activity
                )
            }
        }
    }

    // 2. Call Content
    AddActivityScreenContent(
        selectedParticipants = selectedParticipants,
        eventParticipants = eventParticipants,
        titleInput = titleInput,
        selectedCategory = selectedCategory,
        selectedPayer = selectedPayer,
        isLoading = isLoading,
        onTitleChange = {
            titleInput = it
            viewModel.onTitleChange(it)
        },
        onCategoryChange = { category ->
            selectedCategory = category
            viewModel.onCategoryChange(category)
        },
        onPayerChange = { contact ->
            selectedPayer = contact
            viewModel.onPayerChange(contact)
        },
        onBackClick = handleBackClick,
        onDoneClick = {
            viewModel.saveActivity()
        },
        onAddParticipantClick = { showContactSelectionOverlay = true }
    )
}

data class CategoryOption(
    val name: String,
    val iconRes: Int,
    val colorHex: String = "#FFCC80"
)

// Helper function untuk mendapatkan category option dari nama
fun getCategoryOption(categoryName: String): CategoryOption {
    return when (categoryName) {
        "Food" -> CategoryOption("Food", R.drawable.ic_food_outline, "#FFA726")
        "Shopping" -> CategoryOption("Shopping", R.drawable.ic_cart_outline, "#AB47BC")
        "Transportation" -> CategoryOption("Transportation", R.drawable.ic_car_outline, "#42A5F5")
        "Entertainment" -> CategoryOption("Entertainment", R.drawable.ic_ticket_outline, "#EC407A")
        "Others" -> CategoryOption("Others", R.drawable.ic_other_outline, "#FFCC80")
        else -> CategoryOption("Others", R.drawable.ic_other_outline, "#FFCC80")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreenContent(
    // Data Parameter
    selectedParticipants: List<Contact>,
    eventParticipants: List<Contact>,
    titleInput: String,
    selectedCategory: String,
    selectedPayer: Contact?,
    isLoading: Boolean = false,
    // Event Parameter
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPayerChange: (Contact) -> Unit,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    onAddParticipantClick: () -> Unit
) {
    val payerOptions = remember(eventParticipants) {
        eventParticipants
    }
    var isPayerExpanded by remember { mutableStateOf(false) }

    // 1. Definisikan List Kategori
    val categoryOptions = remember {
        listOf(
            CategoryOption("Food", R.drawable.ic_food_outline, "#FFA726"),
            CategoryOption("Shopping", R.drawable.ic_cart_outline, "#AB47BC"),
            CategoryOption("Transportation", R.drawable.ic_car_outline, "#42A5F5"),
            CategoryOption("Entertainment", R.drawable.ic_ticket_outline, "#EC407A"),
            CategoryOption("Others", R.drawable.ic_other_outline, "#FFCC80")
        )
    }

    // 2. State lokal untuk dropdown
    var isCategoryExpanded by remember { mutableStateOf(false) }

    // 6. Cari object kategori berdasarkan nama yang sedang terpilih
    val currentCategoryIcon = categoryOptions.find { it.name == selectedCategory }?.iconRes

    // 7. State untuk error dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 8. Fungsi validasi
    fun validateInput(): String? {
        return when {
            titleInput.isBlank() -> "Isi Title aktivitas"
            selectedParticipants.isEmpty() -> "Isi minimal 1 participant"
            selectedCategory.isEmpty() -> "Pilih kategori"
            selectedPayer == null -> "Pilih siapa yang membayar"
            selectedParticipants.size == 1 && selectedPayer in selectedParticipants -> "Minimal harus 2 orang berbeda (participant + pembayar)"
            else -> null
        }
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Validation Error", style = AppFont.Bold) },
            text = { Text(errorMessage, style = AppFont.Regular) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow)
                ) {
                    Text("OK", color = UIBlack, style = AppFont.Bold)
                }
            }
        )
    }

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

        // 3. KONTEN AREA
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

                // LIST PARTICIPANT
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    ParticipantAvatarItem(
                        isAddButton = true,
                        onClick = onAddParticipantClick
                    )

                    selectedParticipants.forEach { contact ->
                        ParticipantAvatarItem(
                            name = contact.name,
                            avatarName = contact.avatarName,
                            onClick = { /* Logic remove user */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CATEGORY DROPDOWN
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    DropdownTriggerSection(
                        label = "Category",
                        displayText = selectedCategory.ifEmpty { "Select a Category" },
                        leadingIcon = {
                            val iconRes = currentCategoryIcon ?: R.drawable.ic_other_outline
                            val useDefaultIcon = currentCategoryIcon == null

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(UIGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                if (useDefaultIcon) {
                                    Icon(
                                        imageVector = Icons.Outlined.HelpOutline,
                                        contentDescription = "No category selected",
                                        tint = UIBlack,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = "Category Icon",
                                        tint = UIBlack,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        onClick = { isCategoryExpanded = true }
                    )

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
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
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
                                    onCategoryChange(category.name)
                                    isCategoryExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- PAID BY DROPDOWN (UPDATED TO DICEBEAR) ---
                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    DropdownTriggerSection(
                        label = "Paid by",
                        displayText = selectedPayer?.name ?: "Select participant",
                        leadingIcon = {
                            if (selectedPayer != null) {
                                // 1. Tampilkan Avatar DiceBear (Selected Payer)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data("https://api.dicebear.com/9.x/avataaars/png?seed=${selectedPayer.avatarName}")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Selected Payer Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        placeholder = painterResource(R.drawable.ic_launcher_foreground), // Ganti dengan placeholder yang sesuai
                                        error = painterResource(R.drawable.ic_launcher_foreground) // Ganti dengan icon error yang sesuai
                                    )
                                }
                            } else {
                                // Default Icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(UIGrey),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "No participant selected",
                                        tint = UIBlack,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        onClick = { if (payerOptions.isNotEmpty()) isPayerExpanded = true },
                        enabled = payerOptions.isNotEmpty()
                    )

                    // CUSTOM DROPDOWN MENU
                    DropdownMenu(
                        expanded = isPayerExpanded,
                        onDismissRequest = { isPayerExpanded = false },
                        modifier = Modifier
                            .background(UIWhite)
                            .width(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        payerOptions.forEach { contact ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // 2. Tampilkan Avatar DiceBear (Dropdown Items)
                                        Box(modifier = Modifier.size(32.dp)) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data("https://api.dicebear.com/9.x/avataaars/png?seed=${contact.avatarName}")
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                                error = painterResource(R.drawable.ic_launcher_foreground)
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
                Spacer(modifier = Modifier.height(100.dp))
            }

            // B. STICKY BOTTOM BUTTON
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = "Done",
                    onClick = {
                        val validationError = validateInput()
                        if (validationError != null) {
                            errorMessage = validationError
                            showErrorDialog = true
                        } else {
                            onDoneClick()
                        }
                    },
                    enabled = !isLoading
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
    onClick: () -> Unit,
    enabled: Boolean = true
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
                .clickable(enabled = enabled) { onClick() }
                .alpha(if (enabled) 1f else 0.5f),
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
        Contact(name = "Andi", avatarName = "avatar_1"),
        Contact(name = "Budi", avatarName = "avatar_2")
    )

    MaterialTheme {
        AddActivityScreenContent(
            selectedParticipants = emptyList(),
            eventParticipants = dummyContacts,
            titleInput = "Makan Siang Bareng",
            selectedCategory = "Food",
            selectedPayer = null,
            isLoading = false,
            onTitleChange = {},
            onCategoryChange = {},
            onPayerChange = {},
            onBackClick = {},
            onDoneClick = {},
            onAddParticipantClick = {}
        )
    }
}