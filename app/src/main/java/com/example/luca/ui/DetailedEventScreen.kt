package com.example.luca.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.HeaderSection
import com.example.luca.ui.theme.*

// --- WARNA & FONT KHUSUS ---
val PricePink = Color(0xFFF53B57) // Warna Harga sesuai Figma
val NeutralGrey = Color(0xFFF5F5F5)
val TextDark = Color(0xFF1A1A1A)
val TextLight = Color(0xFF757575)

@Composable
fun DetailedEventScreen(
    onNavigateToAddActivity: () -> Unit = {}
) {
    // Data Dummy
    val activities = remember {
        listOf(
            ActivityData("Simple Breakfast", "Paid by You", "Rp.123.000"),
            ActivityData("Gocar to the Denpasar", "Paid by Steven K", "Rp.92.000"),
            ActivityData("Toll Fee", "Paid by Jeremy E", "Rp.17.000"),
            ActivityData("Barong Shirts", "Paid by Michael K", "Rp.105.000"),
            ActivityData("Beach Entrance Ticket", "Paid by Abel M", "Rp.250.000")
        )
    }

    val isEmpty = activities.isEmpty()

    Scaffold(
        // 1. HEADER PUTIH (PERBAIKAN UTAMA)
        // Kita paksa header punya background putih agar memisah dari kuning di bawahnya
        topBar = {
            Surface(shadowElevation = 0.dp, color = Color.White) {
                HeaderSection()
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAFAFA)) // Background Abu Putih Tulang
        ) {
            // 2. BANNER KUNING (Hanya di area kartu)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Tidak sampai atas, cuma di belakang kartu
                    .background(UIAccentYellow)
            )

            // 3. MAIN CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp)) // Jarak dikit dari Header

                // KARTU EVENT
                FigmaEventCard()

                Spacer(modifier = Modifier.height(20.dp))

                // SEARCH BAR
                FigmaSearchBar()

                Spacer(modifier = Modifier.height(20.dp))

                // LIST / EMPTY STATE
                if (isEmpty) {
                    EmptyStateMessage()
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(activities) { item ->
                            ActivityItemCard(item)
                        }
                    }
                }
            }

            // --- BOTTOM ACTION (Summarize & FAB) ---
            if (!isEmpty) {
                // Gradient Fade halus di bawah
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFFFAFAFA))
                            )
                        )
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
            ) {
                if (isEmpty) {
                    // Tampilan Kosong
                    Column(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("Click here to add one! ↴", color = TextLight, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        FloatingAddButton(onClick = onNavigateToAddActivity)
                    }
                } else {
                    // Tampilan Ada Isi
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Tombol Summarize (Border Kuning)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .height(50.dp)
                                .width(160.dp)
                                .clickable { },
                            shape = RoundedCornerShape(30.dp),
                            color = Color.White,
                            border = BorderStroke(2.dp, UIAccentYellow),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Summarize", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                            }
                        }
                        // Tombol (+)
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            FloatingAddButton(onClick = onNavigateToAddActivity)
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONEN KARTU (RAPID FIX) ---
@Composable
fun FigmaEventCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(280.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. GAMBAR (Placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray) // Ganti ini dengan Image nanti
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
            }

            // 2. TOMBOL CLOSE/EDIT (Top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                SmallCircleButton(icon = Icons.Default.Close, modifier = Modifier.align(Alignment.TopStart))
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    SmallCircleButton(icon = Icons.Default.Edit)
                    Spacer(modifier = Modifier.width(12.dp))
                    SmallCircleButton(icon = Icons.Default.Delete)
                }
            }

            // 3. PANEL INFO PUTIH (Bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Bali with the boys", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark, style = AppFont.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = UIAccentYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buleleng, Bali, Indonesia", color = TextLight, fontSize = 12.sp, style = AppFont.Regular)
                }
            }

            // 4. TANGGAL (Pojok Kanan Bawah Panel Putih)
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 20.dp)) {
                Text("August 24, 2025", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark, style = AppFont.Bold)
            }

            // 5. AVATAR (Numpuk di batas Gambar & Putih)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-20).dp, y = (-70).dp), // Offset ke atas
                horizontalArrangement = Arrangement.spacedBy((-10).dp)
            ) {
                // Placeholder Avatar Abu
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .background(Color.LightGray)
                    )
                }
            }
        }
    }
}

@Composable
fun FigmaSearchBar() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = Color.White,
        shadowElevation = 1.dp // Shadow tipis
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            Icon(Icons.Default.Search, "Search", tint = TextDark, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Search", color = TextLight, fontSize = 16.sp)
        }
    }
}

@Composable
fun ActivityItemCard(item: ActivityData) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icon Background Abu
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(NeutralGrey),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCart, null, tint = TextDark, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark,style = AppFont.Bold)
                Text(item.payer, fontSize = 12.sp, color = TextLight, style = AppFont.Medium)
            }

            // Harga Warna PINK
            Text(item.price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PricePink, style = AppFont.Medium)
        }
    }
}

// ... (Komponen Tombol Kecil & FAB sama seperti sebelumnya) ...
@Composable
fun SmallCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = Color.White, modifier = modifier.size(36.dp).clickable { }) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = TextDark)
        }
    }
}

@Composable
fun FloatingAddButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(60.dp).clickable { onClick() },
        shape = CircleShape,
        color = UIAccentYellow,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, "Add", tint = TextDark, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Oops.", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text("You haven't made any activities.", fontSize = 16.sp, color = TextLight)
    }
}

data class ActivityData(val title: String, val payer: String, val price: String)

@Preview
@Composable
fun DetailedPreview(){
    LucaTheme { DetailedEventScreen() }
}