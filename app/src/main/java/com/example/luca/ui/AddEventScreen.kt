package com.example.luca.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luca.R
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.AddEventViewModel

// ==========================================
// 1. STATEFUL COMPOSABLE (HASIL GABUNGAN)
// Ini tempat bertemunya Logic Si B dan Navigasi Si A
// ==========================================
@Composable
fun AddScreen(
    viewModel: AddEventViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // --- DATA DARI SI B (ViewModel) ---
    val title by viewModel.title.collectAsState()
    val location by viewModel.location.collectAsState()
    val date by viewModel.date.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    // Logic Navigasi balik kalau sukses simpan
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    // Logic Buka Galeri
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    // Panggil Tampilan (UI Si A) dengan Data (Logic Si B)
    AddScreenContent(
        title = title,
        location = location,
        date = date,
        selectedImageUri = selectedImageUri,
        participants = participants,
        isLoading = isLoading,
        onTitleChange = viewModel::onTitleChange,
        onLocationChange = viewModel::onLocationChange,
        onDateChange = viewModel::onDateChange,
        onAddParticipant = viewModel::addParticipant,
        onChangePhotoClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onContinueClick = { viewModel.saveEvent() },
        onBackClick = onNavigateBack
    )
}

// ==========================================
// 2. STATELESS COMPOSABLE (TAMPILAN MURNI SI A)
// ==========================================
@Composable
fun AddScreenContent(
    title: String,
    location: String,
    date: String,
    selectedImageUri: Uri?,
    participants: List<String>,
    isLoading: Boolean,
    onTitleChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onAddParticipant: (String) -> Unit,
    onChangePhotoClick: () -> Unit,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // State lokal untuk Dialog Popup (Ini murni UI interaction, jadi tetap di sini)
    var showDialog by remember { mutableStateOf(false) }
    var newParticipantName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = UIBackground,
        topBar = {
            // Menggunakan HeaderSection desain Si A
            HeaderSection(
                currentState = HeaderState.NEW_EVENT,
                onLeftIconClick = onBackClick
            )
        }
    ) { innerPadding ->

        // --- DIALOG POPUP (UI Si A) ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = UIWhite,
                title = { Text("Add Participant", style = AppFont.Bold, color = UIBlack) },
                text = {
                    OutlinedTextField(
                        value = newParticipantName,
                        onValueChange = { newParticipantName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = UIBlack,
                            unfocusedTextColor = UIBlack,
                            cursorColor = UIAccentYellow,
                            focusedBorderColor = UIAccentYellow
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newParticipantName.isNotBlank()) {
                            onAddParticipant(newParticipantName)
                            newParticipantName = ""
                            showDialog = false
                        }
                    }) {
                        Text("Add", color = UIAccentYellow, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel", color = UIDarkGrey)
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // KONTEN UTAMA
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // --- HERO IMAGE ---
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Cover",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = UIGrey,
                            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "Placeholder Logo",
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap 'Add Photo' to add cover", color = UIDarkGrey, style = AppFont.Regular)
                            }
                        }
                    }

                    // Tombol Ganti Foto
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = UIWhite,
                        shadowElevation = 4.dp,
                        onClick = onChangePhotoClick
                    ) {
                        Text(
                            text = if (selectedImageUri == null) "Add Photo" else "Change Photo",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            style = AppFont.SemiBold,
                            color = UIBlack
                        )
                    }
                }

                // --- INPUT FORM ---
                Column(modifier = Modifier.padding(20.dp)) {

                    InputSection(
                        label = "Title", value = title, placeholder = "New Event",
                        testTag = "input_title", onValueChange = onTitleChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    InputSection(
                        label = "Location", value = location, placeholder = "Event Location",
                        testTag = "input_location", onValueChange = onLocationChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    InputSection(
                        label = "Date", value = date, placeholder = "Event Date",
                        testTag = "input_date", onValueChange = onDateChange
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- PARTICIPANTS LIST ---
                    Text(
                        text = "Participants",
                        style = AppFont.SemiBold,
                        fontSize = 16.sp,
                        color = UIBlack,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        item { ParticipantItem(name = "You", isYou = true) }

                        items(participants) { name ->
                            ParticipantItem(name = name)
                        }

                        item {
                            Box(modifier = Modifier.clickable { showDialog = true }) {
                                ParticipantItem(name = "", isAddButton = true)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // --- BUTTON CONTINUE ---
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        PrimaryButton(
                            text = if (isLoading) "Saving..." else "Continue",
                            onClick = { if (!isLoading) onContinueClick() }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // --- LOADING OVERLAY ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = UIAccentYellow)
                }
            }
        }
    }
}

// Preview khusus tampilan (Pakai dummy data)
@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    LucaTheme {
        AddScreenContent(
            title = "", location = "", date = "",
            selectedImageUri = null, participants = listOf("Budi", "Siti"),
            isLoading = false,
            onTitleChange = {}, onLocationChange = {}, onDateChange = {},
            onAddParticipant = {}, onChangePhotoClick = {}, onContinueClick = {}, onBackClick = {}
        )
    }
}