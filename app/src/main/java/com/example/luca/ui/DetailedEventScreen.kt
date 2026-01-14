package com.example.luca.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.HeaderSection
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme

// Definisi Warna Hardcoded sesuai request (bisa dipindah ke Color.kt nanti)
val UiAccentYellow = Color(0xFFFFD54F) // Warna sample kuning
val PricePink = Color(0xFFF53B57)
val TextGray = Color(0xFF808080)

@Composable
fun DetailedEventScreen(
    onBackClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onAddActivityClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            HeaderSection(
                onLeftIconClick = onBackClick
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Content Layout (Tidak scroll - hanya LazyColumn yang scroll)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp) // PENTING: Padding bawah agar item terakhir tidak tertutup Bottom Button
            ) {
                // Placeholder Components - Static (tidak scroll)
                EventCard()
                Spacer(modifier = Modifier.height(16.dp))
                SearchBarModify(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(50.dp),
                    placeholder = "Search",
                    onSearchQueryChange = { query ->
                        // Handle search query change
                        println("Search: $query")
                    },
                    readOnly = false
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Activity List Section - Hanya bagian ini yang scrollable
                ActivitySection(onActivityClick = onActivityClick)
            }

            // 3. Bottom Action Area (Sticky at Bottom)
            // Gradient Fade Overlay - Membuat konten terlihat samar di bawah tombol
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(120.dp) // Tinggi area fade
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent, // Atas transparan
                                Color.White.copy(alpha = 0.3f), // Mulai fade
                                Color.White.copy(alpha = 0.7f), // Makin solid
                                Color.White // Bawah solid putih
                            )
                        )
                    )
            )

            BottomActionArea(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                onAddActivityClick = onAddActivityClick
            )
        }
    }
}

//Activity Section
@Composable
fun ActivitySection(onActivityClick: () -> Unit = {}) {
    // Data dummy dipindah kesini agar rapi
    val activities = listOf(
        ActivityData(Color(0xFFFF9800), "Simple Breakfast", "Paid by You", "Rp.123.000"),
        ActivityData(Color(0xFF4CAF50), "Gocar to Denpasar", "Paid by Steven K", "Rp.92.000"),
        ActivityData(Color(0xFF2196F3), "Toll Fee", "Paid by Jeremy E", "Rp.17.000"),
        ActivityData(Color(0xFFE91E63), "Barong Shirts", "Paid by Michael K", "Rp.105.000"),
        ActivityData(Color(0xFF9C27B0), "Beach Ticket", "Paid by You", "Rp.250.000"),
        ActivityData(Color(0xFFFF5722), "Dinner Jimbaran", "Paid by Steven K", "Rp.500.000")
    )

    // Menggunakan LazyColumn untuk scrollable list yang efisien
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), // Mengisi sisa tinggi yang tersedia
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Jarak antar card vertikal
    ) {
        items(activities) { item ->
            ActivityItemCard(
                iconColor = item.color,
                title = item.title,
                payer = item.payer,
                price = item.price,
                onClick = onActivityClick
            )
        }
    }
}

@Composable
//Activity item
fun ActivityItemCard(
    iconColor: Color,
    title: String,
    payer: String,
    price: String,
    onClick: () -> Unit = {}
) {
    // Menggunakan Surface untuk efek Card Putih + Shadow (Floating)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), // Tinggi fixed agar rapi (opsional, bisa wrap_content)
        shape = RoundedCornerShape(24.dp), // Sangat bulat
        color = Color.White,
        shadowElevation = 6.dp, // Efek bayangan agar 'pop'
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icon (Kiri) - Diganti dengan circle berwarna
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor, CircleShape) // Circle solid dengan warna penuh
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Text Content (Tengah)
            Column(
                modifier = Modifier.weight(1f) // Mendorong harga ke kanan mentok
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    style = AppFont.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = payer,
                    fontSize = 12.sp,
                    style = AppFont.Medium,
                    color = TextGray
                )
            }

            // 3. Price (Kanan)
            Text(
                text = price,
                fontSize = 16.sp,
                style = AppFont.SemiBold,
                color = PricePink // Warna merah/pink khusus
            )
        }
    }
}

@Composable
fun BottomActionArea(
    modifier: Modifier = Modifier,
    onAddActivityClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Tombol Summarize - Di tengah
        Surface(
            modifier = Modifier
                .height(50.dp)
                .width(200.dp)
                .align(Alignment.Center), // Posisi tengah
            shape = RoundedCornerShape(50), // Pill Shape
            color = Color.White,
            border = BorderStroke(2.dp, UiAccentYellow),
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Summarize",
                    style = AppFont.SemiBold,
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }
        }

        // Tombol FAB (+) - Mepet kanan
        Surface(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.CenterEnd), // Posisi kanan
            shape = CircleShape,
            color = UiAccentYellow,
            shadowElevation = 6.dp,
            onClick = onAddActivityClick
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Activity",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// --- Data Helper ---
data class ActivityData(
    val color: Color,
    val title: String,
    val payer: String,
    val price: String
)

@Preview(showBackground = true)
@Composable
fun DetailedEventPreview(){
    LucaTheme {
        DetailedEventScreen()
    }
}