package com.example.luca

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBackground
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIWhite
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIDarkGrey

// Dummy Data
data class Participant(val id: Int, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen() {
    // State
    var titleInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }

    val participants = remember {
        listOf(
            Participant(1, "Jeremy E"),
            Participant(2, "Steven K"),
            Participant(3, "Michael K"),
            Participant(4, "Abe"),
            Participant(5, "Sarah L"),
        )
    }

    Scaffold(
        containerColor = UIBackground, // Menggunakan UIBackground (Cream/Putih Tulang)
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "New Event", // Literal String
                        style = AppFont.SemiBold,
                        fontWeight = FontWeight.Bold,
                        color = UIBlack
                    )
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
                    .height(220.dp)
                    .background(UIGrey) // Menggunakan UIGrey
            ) {
                Text(
                    text = "Image Placeholder", // Literal String
                    color = UIDarkGrey,
                    modifier = Modifier.align(Alignment.Center)
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = UIWhite
                ) {
                    Text(
                        text = "Change Photo", // Literal String
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        style = AppFont.SemiBold,
                        color = UIBlack
                    )
                }
            }

            // --- 2. Form & Inputs ---
            Column(modifier = Modifier.padding(16.dp)) {

                InputSection(
                    label = "Title", // Literal String
                    value = titleInput,
                    placeholder = "Snorkeling Trip", // Literal String
                    testTag = "input_title",
                    onValueChange = { titleInput = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputSection(
                    label = "Location", // Literal String
                    value = locationInput,
                    placeholder = "Thousand Islands, Indonesia", // Literal String
                    testTag = "input_location",
                    onValueChange = { locationInput = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputSection(
                    label = "Date", // Literal String
                    value = dateInput,
                    placeholder = "December 20, 2025", // Literal String
                    testTag = "input_date",
                    onValueChange = { dateInput = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Participants ---
                Text(
                    text = "Participants", // Literal String
                    style = AppFont.SemiBold,
                    fontSize = 16.sp,
                    color = UIBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    item { ParticipantItem(name = "You", isYou = true) } // Literal String
                    items(participants) { person -> ParticipantItem(name = person.name) }
                    item { ParticipantItem(name = "", isAddButton = true) }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Button Continue ---
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow), // Menggunakan Kuning Project
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Continue", // Literal String
                        color = UIBlack, // Text hitam di atas kuning
                        style = AppFont.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    LucaTheme {
        AddScreen()
    }
}