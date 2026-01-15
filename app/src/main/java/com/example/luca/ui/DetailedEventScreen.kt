package com.example.luca.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.HeaderSection
import com.example.luca.R
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.DetailedEventViewModel
import com.example.luca.viewmodel.UIActivityState
import com.example.luca.viewmodel.UIEventState

@Composable
fun DetailedEventScreen(
    onBackClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onAddActivityClick: () -> Unit = {}
) {
fun DetailedEventScreen(
    eventId: String = "1", // Nanti ID ini didapat dari navigasi (diklik dari Home)
    viewModel: DetailedEventViewModel = viewModel(),
    onNavigateToAddActivity: () -> Unit = {}
) {
    // 1. Suruh ViewModel ambil data berdasarkan ID
    LaunchedEffect(eventId) {
        viewModel.loadEventData(eventId)
    }

    // 2. Pantau Data (Collect)
    val eventState by viewModel.uiEvent.collectAsState()
    val activitiesState by viewModel.uiActivities.collectAsState()
    val isEmpty = activitiesState.isEmpty()

    Scaffold(
        topBar = {
            HeaderSection(
                onLeftIconClick = onBackClick
            )
            Surface(shadowElevation = 0.dp, color = UIWhite) {
                HeaderSection()
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(UIBackground)
        ) {
            // Banner Kuning
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
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(UIAccentYellow)
            )

            // Konten
            Column(modifier = Modifier.fillMaxSize()) {
                FigmaEventCard(event = eventState)
            BottomActionArea(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                onAddActivityClick = onAddActivityClick
            )
        }
    }
}

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    FigmaSearchBar()
                    Spacer(modifier = Modifier.height(20.dp))
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

                    if (isEmpty) {
                        EmptyStateMessage()
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(activitiesState) { activity ->
                                ActivityItemCard(activity)
                            }
                        }
                    }
                }
            }
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

            // Bottom Gradient & Buttons
            if (!isEmpty) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(100.dp)
                        .background(Brush.verticalGradient(colors = listOf(Color.Transparent, UIBackground)))
                )
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
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp, start = 20.dp, end = 20.dp).fillMaxWidth()
            ) {
                if (isEmpty) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("Click here to add one! ↴", color = UIDarkGrey, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        FloatingAddButton(onClick = onNavigateToAddActivity)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center).height(50.dp).width(160.dp).clickable { },
                            shape = RoundedCornerShape(30.dp), color = UIWhite, border = BorderStroke(2.dp, UIAccentYellow), shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Summarize", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIBlack)
                            }
                        }
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            FloatingAddButton(onClick = onNavigateToAddActivity)
                        }
                    }
                }
            }
        }
    }
}

// ... (Komponen UI Lain: FigmaEventCard, ActivityItemCard, dll SAMA SEPERTI SEBELUMNYA) ...
// (Tidak perlu saya tulis ulang agar hemat tempat, karena isinya sama persis)
@Composable
fun FigmaEventCard(event: UIEventState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 20.dp, end = 20.dp).height(280.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (event.imageRes != null) {
                Image(
                    painter = painterResource(id = event.imageRes),
                    contentDescription = "Event Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp).align(Alignment.TopCenter)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.Gray).align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter)) {
                SmallCircleButton(Icons.Default.Close, Modifier.align(Alignment.TopStart))
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    SmallCircleButton(Icons.Default.Edit)
                    Spacer(modifier = Modifier.width(12.dp))
                    SmallCircleButton(Icons.Default.Delete)
                }
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(topStart = 40.dp), color = UIWhite
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = UIBlack, style = AppFont.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = UIAccentYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.location, color = UIDarkGrey, fontSize = 12.sp, style = AppFont.Regular)
                        }
                    }
                    Text(
                        text = event.date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UIBlack, style = AppFont.Bold,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-20).dp, y = (-70).dp),
                horizontalArrangement = Arrangement.spacedBy((-10).dp)
            ) {
                event.participantColors.forEach { color ->
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).border(2.dp, UIWhite, CircleShape).background(color))
                }
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
@Composable
fun ActivityItemCard(item: UIActivityState) {
    Surface(
        shape = RoundedCornerShape(20.dp), color = UIWhite, shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(item.iconColor.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ShoppingCart, null, tint = UIBlack, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIBlack, style = AppFont.Bold)
                Text(item.payer, fontSize = 12.sp, color = UIDarkGrey, style = AppFont.Medium)
            }
            Text(item.price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIAccentRed, style = AppFont.Medium)
        }
    }
}

@Composable
fun FigmaSearchBar() {
    Surface(shape = RoundedCornerShape(25.dp), color = UIWhite, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth().height(50.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            Icon(Icons.Default.Search, null, tint = UIBlack, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Search", color = UIDarkGrey, fontSize = 16.sp)
        }
    }
}

@Composable
fun SmallCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = UIWhite, modifier = modifier.size(36.dp).clickable { }) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = UIBlack)
        }
    }
}

@Composable
fun FloatingAddButton(onClick: () -> Unit) {
    Surface(modifier = Modifier.size(60.dp).clickable { onClick() }, shape = CircleShape, color = UIAccentYellow, shadowElevation = 6.dp) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, null, tint = UIBlack, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Oops.", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = UIDarkGrey)
        Spacer(modifier = Modifier.height(8.dp))
        Text("You haven't made any activities.", fontSize = 16.sp, color = UIDarkGrey)
    }
}

@Preview
@Composable
fun DetailedPreview() {
    LucaTheme {
        DetailedEventScreen()
    }
}