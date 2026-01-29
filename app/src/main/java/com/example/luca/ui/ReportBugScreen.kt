package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBugScreen(onBackClick: () -> Unit) {
    // State untuk form input (UI Only)
    var subjectText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Report a Bug",
                        style = AppFont.Bold,
                        fontSize = 20.sp,
                        color = UIBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = UIBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UIWhite,
                    scrolledContainerColor = UIWhite
                )
            )
        },
        containerColor = UIBackground,
        bottomBar = {
            // Sticky Bottom Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UIBackground)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = { /* TODO: Submit Logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UIAccentYellow,
                        contentColor = UIBlack
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Submit Report",
                        style = AppFont.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {

            // ===== 1. HEADER ILUSTRASI / INTRO =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(UIAccentYellow.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = UIBlack, // Atau warna aksen gelap
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Found an issue?",
                    style = AppFont.Bold,
                    fontSize = 18.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please describe the bug you encountered. Your feedback helps us make Luca better.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // ===== 2. FORM SECTION =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                // Input: Subject
                Text(
                    text = "Subject",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                LucaTextField(
                    value = subjectText,
                    onValueChange = { subjectText = it },
                    placeholder = "e.g., App crashes on split screen"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input: Description
                Text(
                    text = "Description",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                LucaTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = "Tell us what happened, steps to reproduce, etc.",
                    singleLine = false,
                    minLines = 5
                )
            }

            // ===== 3. ATTACHMENT SECTION =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Screenshots (Optional)",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload images to help us understand the issue.",
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Add Button Visual
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Upload (Placeholder)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(UIGrey.copy(alpha = 0.3f))
                            .clickable { /* TODO: Open Gallery */ }
                            .border(1.dp, UIDarkGrey.copy(alpha = 0.5f), RoundedCornerShape(12.dp)), // Efek border tipis
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                tint = UIDarkGrey
                            )
                            Text(
                                text = "Add",
                                style = AppFont.Regular,
                                fontSize = 10.sp,
                                color = UIDarkGrey
                            )
                        }
                    }

                    // Contoh jika ada gambar yang sudah diupload (Visual Only)
                    // Box(
                    //     modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray)
                    // ) { ... }
                }
            }

            // Spacer untuk memberi ruang scroll agar tidak tertutup tombol sticky
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ==========================================
// CUSTOM TEXT FIELD (Agar konsisten & bersih)
// ==========================================

@Composable
fun LucaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = AppFont.Regular,
                color = UIDarkGrey.copy(alpha = 0.6f)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = AppFont.Regular.fontFamily,
            fontSize = 14.sp,
            color = UIBlack
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UIAccentYellow,
            unfocusedBorderColor = UIGrey,
            focusedContainerColor = UIWhite,
            unfocusedContainerColor = UIWhite,
            cursorColor = UIBlack
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        singleLine = singleLine,
        minLines = minLines
    )
}

@Preview
@Composable
fun ReportBugsPreview() {
    LucaTheme {
        ReportBugScreen(onBackClick = {})
    }
}