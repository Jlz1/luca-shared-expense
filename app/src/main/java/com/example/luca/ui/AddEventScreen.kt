package com.example.luca.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll // PENTING: Import scroll horizontal
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luca.R
import com.example.luca.model.Contact
import com.example.luca.ui.components.*
import com.example.luca.ui.theme.*
import com.example.luca.util.AvatarUtils
import com.example.luca.viewmodel.AddEventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddScreen(
    viewModel: AddEventViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val location by viewModel.location.collectAsState()
    val date by viewModel.date.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    // DATA DARI VIEWMODEL
    val availableContacts by viewModel.availableContacts.collectAsState()
    val selectedParticipants by viewModel.selectedParticipants.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    // STATE UNTUK OVERLAY
    var showContactSelectionOverlay by remember { mutableStateOf(false) }
    var showAddNewContactOverlay by remember { mutableStateOf(false) }

    AddScreenContent(
        title = title,
        location = location,
        date = date,
        selectedImageUri = selectedImageUri,
        selectedParticipants = selectedParticipants,
        isLoading = isLoading,
        onTitleChange = viewModel::onTitleChange,
        onLocationChange = viewModel::onLocationChange,
        onDateChange = viewModel::onDateChange,
        onChangePhotoClick = {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onAddParticipantClick = { showContactSelectionOverlay = true }, // Buka Overlay
        onContinueClick = { viewModel.saveEvent() },
        onBackClick = onNavigateBack
    )

    // --- OVERLAY 1: SELECT CONTACTS ---
    if (showContactSelectionOverlay) {
        Dialog(
            onDismissRequest = { showContactSelectionOverlay = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
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

    // --- OVERLAY 2: ADD NEW CONTACT ---
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreenContent(
    title: String,
    location: String,
    date: String,
    selectedImageUri: Uri?,
    selectedParticipants: List<Contact>,
    isLoading: Boolean,
    onTitleChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onChangePhotoClick: () -> Unit,
    onAddParticipantClick: () -> Unit,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        // FIX ERROR: Fungsi helper sekarang ada di bawah file ini
                        onDateChange(convertMillisToDate(millis))
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Column(modifier = Modifier.fillMaxSize().background(UIBackground)) {
        HeaderSection(currentState = HeaderState.NEW_EVENT, onLeftIconClick = onBackClick)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                // HERO IMAGE
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Cover",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(modifier = Modifier.fillMaxSize(), color = UIDarkGrey) {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(64.dp), tint = UIWhite)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap 'Add Photo' to add cover", color = UIWhite, style = AppFont.Regular)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = UIWhite,
                        shadowElevation = 4.dp,
                        onClick = onChangePhotoClick
                    ) {
                        Text(if (selectedImageUri == null) "Add Photo" else "Change Photo", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 12.sp, style = AppFont.SemiBold, color = UIBlack)
                    }
                }

                // INPUT FORM
                Column(modifier = Modifier.padding(20.dp)) {
                    InputSection(label = "Title", value = title, placeholder = "New Event", testTag = "input_title", onValueChange = onTitleChange)
                    Spacer(modifier = Modifier.height(16.dp))
                    InputSection(label = "Location", value = location, placeholder = "Event Location", testTag = "input_location", onValueChange = onLocationChange)
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        InputSection(label = "Date", value = date, placeholder = "Event Date", testTag = "input_date", onValueChange = {})
                        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // PARTICIPANTS SECTION
                    Text("Participants", style = AppFont.SemiBold, fontSize = 16.sp, color = UIBlack, modifier = Modifier.padding(bottom = 12.dp))

                    // --- PERBAIKAN: TAMBAH HORIZONTAL SCROLL ---
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()) // Scroll horizontal aktif
                    ) {
                        ParticipantAvatarItem(isAddButton = true, onClick = onAddParticipantClick)

                        selectedParticipants.forEach { contact ->
                            ParticipantAvatarItem(
                                name = contact.name,
                                avatarName = contact.avatarName,
                                onClick = { /* Logic remove user if needed */ }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        PrimaryButton(text = if (isLoading) "Saving..." else "Continue", onClick = { if (!isLoading) onContinueClick() })
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UIAccentYellow)
                }
            }
        }
    }
}

// --- HELPER COMPONENTS & FUNCTIONS ---

@Composable
fun ParticipantAvatarItem(
    name: String = "",
    avatarName: String = "",
    isAddButton: Boolean = false,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier.size(60.dp).clip(CircleShape).background(if (isAddButton) UIGrey else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isAddButton) {
                Icon(Icons.Default.Add, null, tint = UIBlack)
            } else {
                Image(
                    painter = painterResource(id = AvatarUtils.getAvatarResId(avatarName)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
        }
        if (!isAddButton) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = name, fontSize = 10.sp, style = AppFont.Regular, maxLines = 1, color = UIBlack)
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}