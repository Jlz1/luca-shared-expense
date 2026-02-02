package com.example.luca.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.model.Contact
import com.example.luca.ui.theme.*
import java.util.Locale

// Dummy Model untuk Settlement
data class Settlement(
    val id: String,
    val from: Contact,
    val to: Contact,
    val amount: Long,
    var isPaid: Boolean = false
)

enum class SummaryTab {
    SETTLEMENT, DETAILS
}

@Composable
fun SummaryScreen(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    // State Tab
    var currentTab by remember { mutableStateOf(SummaryTab.SETTLEMENT) }

    // Dummy Data Settlement
    val settlements = remember {
        mutableStateListOf(
            Settlement("1", Contact(name = "Jeremy E", avatarName = "avatar_2"), Contact(name = "You", avatarName = "avatar_1"), 150000),
            Settlement("2", Contact(name = "Abel M", avatarName = "avatar_3"), Contact(name = "You", avatarName = "avatar_1"), 75000),
            Settlement("3", Contact(name = "Jeremy E", avatarName = "avatar_2"), Contact(name = "Abel M", avatarName = "avatar_3"), 25000),
            Settlement("4", Contact(name = "John D", avatarName = ""), Contact(name = "You", avatarName = "avatar_1"), 12000),
            Settlement("5", Contact(name = "John D", avatarName = ""), Contact(name = "You", avatarName = "avatar_1"), 12000),
            Settlement("6", Contact(name = "John D", avatarName = ""), Contact(name = "You", avatarName = "avatar_1"), 12000),
        )
    }

    // Container Utama (Kuning)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {
        // 1. Header
        // Menggunakan HeaderSection yang sudah ada (pastikan import sesuai package kamu)
        // Disini saya mock manual jika HeaderSection belum di import
        HeaderSection(currentState = HeaderState.SUMMARY, onLeftIconClick = onBackClick)

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Content Box (Putih Rounded)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(UIBackground)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- TAB SWITCHER ---
                CustomSegmentedControl(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- CONTENT BASED ON TAB ---
                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        SummaryTab.SETTLEMENT -> {
                            SettlementList(
                                settlements = settlements,
                                onTogglePaid = { settlement ->
                                    // Update logic dummy
                                    val index = settlements.indexOfFirst { it.id == settlement.id }
                                    if (index != -1) {
                                        settlements[index] = settlements[index].copy(isPaid = !settlements[index].isPaid)
                                    }
                                }
                            )
                        }
                        SummaryTab.DETAILS -> {
                            SpreadsheetPlaceholder()
                        }
                    }
                }
            }

            // --- BOTTOM SHARE BUTTON ---
            // Hanya muncul di tab Settlement biar gampang di SS
            if (currentTab == SummaryTab.SETTLEMENT) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 34.dp, start = 20.dp, end = 20.dp)
                ) {
                    Button(
                        onClick = onShareClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIBlack, // Hitam biar kontras sama kartu putih
                            contentColor = UIWhite
                        ),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Share Settlement Plan", style = AppFont.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// --- KOMPONEN: CUSTOM TAB ---
@Composable
fun CustomSegmentedControl(
    currentTab: SummaryTab,
    onTabSelected: (SummaryTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(UIWhite)
            .border(1.dp, UIGrey.copy(alpha = 0.2f), RoundedCornerShape(25.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            SummaryTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val textColor = if (isSelected) UIBlack else UIDarkGrey
                val weight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(if (isSelected) UIAccentYellow else Color.Transparent)
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tab == SummaryTab.SETTLEMENT) "Settlement" else "Details",
                        color = textColor,
                        fontWeight = weight,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- KOMPONEN: SETTLEMENT LIST ---
@Composable
fun SettlementList(
    settlements: List<Settlement>,
    onTogglePaid: (Settlement) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settlements",
                style = AppFont.SemiBold,
                fontSize = 18.sp,
                color = UIBlack,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
        }

        items(settlements) { item ->
            SettlementCard(item = item, onTogglePaid = { onTogglePaid(item) })
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "All calculations are optimized to minimize the number of transactions.",
                style = AppFont.Regular,
                fontSize = 12.sp,
                color = UIDarkGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- KOMPONEN: KARTU SETTLEMENT ---
@Composable
fun SettlementCard(
    item: Settlement,
    onTogglePaid: () -> Unit
) {
    val alpha = if (item.isPaid) 0.5f else 1f
    val containerColor = if (item.isPaid) UIDarkGrey.copy(alpha = 0.1f) else UIWhite
    val borderColor = if (item.isPaid) Color.Transparent else UIAccentYellow.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onTogglePaid() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Kiri: Avatar Flow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            // From Avatar
            ParticipantAvatarItemSmall(item.from)

            // Arrow
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(UIGrey.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Pays to",
                    tint = UIBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            // To Avatar
            ParticipantAvatarItemSmall(item.to)
        }

        // Kanan: Jumlah & Checkbox
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Rp${String.format(Locale.getDefault(), "%,.0f", item.amount.toDouble())}",
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = if (item.isPaid) UIDarkGrey else UIBlack,
                textDecoration = if (item.isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isPaid) {
                    Text(
                        text = "PAID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50) // Green
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Text(
                        text = "UNPAID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE57373) // Red-ish
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Custom Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (item.isPaid) Color(0xFF4CAF50) else Color.Transparent)
                .border(2.dp, if (item.isPaid) Color.Transparent else UIGrey, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (item.isPaid) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = UIWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// --- KOMPONEN: PLACEHOLDER SPREADSHEET ---
@Composable
fun SpreadsheetPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.TableChart,
            contentDescription = null,
            tint = UIAccentYellow,
            modifier = Modifier
                .size(80.dp)
                .padding(top = 40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Detailed Transactions",
            style = AppFont.SemiBold,
            fontSize = 20.sp,
            color = UIBlack
        )
        Text(
            text = "A full spreadsheet view of every item and split will be available here.",
            style = AppFont.Regular,
            fontSize = 14.sp,
            color = UIDarkGrey,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mock Table Visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(UIGrey.copy(alpha = 0.1f))
                .border(1.dp, UIGrey.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            // Garis-garis dummy spreadsheet
            Column {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, UIGrey.copy(alpha = 0.1f))
                    )
                }
            }
            Text(
                text = "Spreadsheet View Placeholder",
                modifier = Modifier.align(Alignment.Center),
                color = UIDarkGrey.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview() {
    LucaTheme {
        SummaryScreen()
    }
}