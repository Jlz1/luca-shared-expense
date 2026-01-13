package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBackground
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

data class Participant(val id: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen() {
    // State
    var titleInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }

    val participants = remember { mutableStateListOf<Participant>() }

    Scaffold(
        containerColor = UIBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "New Event",
                        style = AppFont.SemiBold,
                        fontWeight = FontWeight.Bold,
                        color = UIBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle Back */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = UIBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = UIBackground
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // --- 1. Hero Image Placeholder ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
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

                        Text(
                            text = "Tap to add cover photo",
                            color = UIDarkGrey,
                            style = AppFont.Regular
                        )
                    }
                }

                // Tombol Change Photo
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = UIWhite,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "Change Photo",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        style = AppFont.SemiBold,
                        color = UIBlack
                    )
                }
            }

            // --- 2. Form & Inputs ---
            Column(modifier = Modifier.padding(20.dp)) {

                InputSection(
                    label = "Title",
                    value = titleInput,
                    placeholder = "New Event",
                    testTag = "input_title",
                    onValueChange = { titleInput = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputSection(
                    label = "Location",
                    value = locationInput,
                    placeholder = "Event Location (Optional)",
                    testTag = "input_location",
                    onValueChange = { locationInput = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputSection(
                    label = "Date",
                    value = dateInput,
                    placeholder = "Event Date (Optional)",
                    testTag = "input_date",
                    onValueChange = { dateInput = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Participants ---
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
                    items(participants) { person -> ParticipantItem(name = person.name) }
                    item { ParticipantItem(name = "", isAddButton = true) }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- Button Continue ---
                // REVISI: Modifier.fillMaxWidth() dihapus dari PrimaryButton
                // Box tetap ada untuk memastikan button berada di tengah (Center)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PrimaryButton(
                        text = "Continue",
                        onClick = { /* Handle Continue Action */ }
                        // Kita TIDAK menambahkan modifier fillMaxWidth di sini
                        // sehingga ukurannya kembali ke default 220.dp (pendek)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(
    showBackground = true,
    apiLevel = 36
)
@Composable
fun AddScreenPreview() {
    LucaTheme {
        AddScreen()
    }
}