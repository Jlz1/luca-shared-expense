package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBackground
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    var titleInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select a Category") }
    var selectedPayer by remember { mutableStateOf("Payer") }

    Scaffold(
        containerColor = UIBackground,
        topBar = {
            HeaderSection(
                currentState = HeaderState.NEW_ACTIVITY,
                onLeftIconClick = onBackClick
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UIBackground)
                    .padding(20.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- PENGGUNAAN KOMPONEN BARU DI SINI ---
                PrimaryButton(
                    text = "Continue",
                    onClick = onContinueClick
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            InputSection(
                label = "Title",
                value = titleInput,
                placeholder = "New Activity",
                testTag = "input_activity_title",
                onValueChange = { titleInput = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Participants",
                style = AppFont.SemiBold,
                fontSize = 16.sp,
                color = UIBlack,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { ParticipantItem(name = "You", isYou = true) }
                item { ParticipantItem(name = "", isAddButton = true) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DropdownTriggerSection(
                label = "Category",
                displayText = selectedCategory,
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(UIGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Category Icon",
                            tint = UIBlack
                        )
                    }
                },
                onClick = { }
            )

            Spacer(modifier = Modifier.height(24.dp))

            DropdownTriggerSection(
                label = "Paid by",
                displayText = selectedPayer,
                leadingIcon = {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Payer Icon",
                            tint = UIDarkGrey,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                onClick = { }
            )

            Spacer(modifier = Modifier.height(20.dp))
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
    apiLevel = 36
)
@Composable
fun AddActivityScreenPreview() {
    LucaTheme {
        AddActivityScreen()
    }
}