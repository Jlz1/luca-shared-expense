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
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.DetailedEventViewModel
import com.example.luca.viewmodel.UIActivityState
import com.example.luca.viewmodel.UIEventState

data class ActivityData(
    val color: Color,
    val title: String,
    val payer: String,
    val price: String
)

@Composable
fun DetailedEventScreen(
    eventId: String = "1",
    viewModel: DetailedEventViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToAddActivity: () -> Unit = {}
) {
    // 1. Load event data based on ID
    LaunchedEffect(eventId) {
        viewModel.loadEventData(eventId)
    }

    // 2. Hoist state from ViewModel
    val eventState by viewModel.uiEvent.collectAsState()
    val activitiesState by viewModel.uiActivities.collectAsState()
    val isEmpty = activitiesState.isEmpty()

    // 3. STRUKTUR UTAMA (No Scaffold)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow) // Background Kuning Global
            .statusBarsPadding() // Agar tidak ketutup status bar
    ) {

        // 4. HEADER (Manual Placement)
        // Surface opsional, tapi HeaderSection biasanya sudah punya background transparan/kuning
        HeaderSection(onLeftIconClick = onBackClick)

        // 5. CONTENT CONTAINER (White Area)
        Box(
            modifier = Modifier
                .weight(1f) // Mengisi sisa ruang ke bawah
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) // Melengkung atas
                .background(UIBackground)
        ) {
            // SCROLLABLE CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                // Tidak perlu padding bottom besar di sini, spacer nanti yang ngurus
            ) {
                // Tambahkan padding horizontal untuk konten di dalam
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Event Card
                    FigmaEventCard(event = eventState)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Bar
                    FigmaSearchBar()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Activity List Section
                    // Pastikan ActivitySection handle scrolling atau gunakan LazyColumn di dalamnya
                    ActivitySection(
                        activities = activitiesState,
                        isEmpty = isEmpty,
                        onNavigateToAddActivity = onNavigateToAddActivity
                    )

                    // Spacer agar konten paling bawah tidak ketutup tombol floating
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            // --- FLOATING ELEMENTS (Overlay) ---

            // Bottom Gradient Overlay (Pemanis biar tombol gak kaku)
            if (!isEmpty) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, UIBackground)
                            )
                        )
                )
            }

            // Bottom Action Area (Tombol Add / Total Bill)
            // Ditaruh di sini agar melayang di atas list
            BottomActionArea(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                isEmpty = isEmpty,
                onAddActivityClick = onNavigateToAddActivity
            )
        }
    }
}

@Composable
fun ActivitySection(
    activities: List<UIActivityState>,
    isEmpty: Boolean,
    onNavigateToAddActivity: () -> Unit = {}
) {
    if (isEmpty) {
        EmptyStateMessage(onNavigateToAddActivity = onNavigateToAddActivity)
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activities) { activity ->
                ActivityItemCard(item = activity)
            }
        }
    }
}

@Composable
fun ActivityItemCard(item: UIActivityState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = UIWhite,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    null,
                    tint = UIBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = UIBlack,
                    style = AppFont.Bold
                )
                Text(
                    item.payer,
                    fontSize = 12.sp,
                    color = UIDarkGrey,
                    style = AppFont.Medium
                )
            }
            Text(
                item.price,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = UIAccentRed,
                style = AppFont.Medium
            )
        }
    }
}

@Composable
fun FigmaEventCard(event: UIEventState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 20.dp, end = 20.dp)
            .height(280.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (event.imageRes != null) {
                Image(
                    painter = painterResource(id = event.imageRes),
                    contentDescription = "Event Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.TopCenter)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                SmallCircleButton(Icons.Default.Close, Modifier.align(Alignment.TopStart))
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    SmallCircleButton(Icons.Default.Edit)
                    Spacer(modifier = Modifier.width(12.dp))
                    SmallCircleButton(Icons.Default.Delete)
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(topStart = 40.dp),
                color = UIWhite
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            event.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = UIBlack,
                            style = AppFont.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = UIAccentYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                event.location,
                                color = UIDarkGrey,
                                fontSize = 12.sp,
                                style = AppFont.Regular
                            )
                        }
                    }
                    Text(
                        text = event.date,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = UIBlack,
                        style = AppFont.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-20).dp, y = (-70).dp),
                horizontalArrangement = Arrangement.spacedBy((-10).dp)
            ) {
                event.participantColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, UIWhite, CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomActionArea(
    modifier: Modifier = Modifier,
    isEmpty: Boolean,
    onAddActivityClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxWidth()) {
        if (isEmpty) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text("Click here to add one! ↴", color = UIDarkGrey, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FloatingAddButton(onClick = onAddActivityClick)
            }
        } else {
            Surface(
                modifier = Modifier
                    .height(50.dp)
                    .width(160.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(30.dp),
                color = UIWhite,
                border = BorderStroke(2.dp, UIAccentYellow),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Summarize",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = UIBlack
                    )
                }
            }
            FloatingAddButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = onAddActivityClick
            )
        }
    }
}

@Composable
fun FigmaSearchBar() {
    Surface(
        shape = RoundedCornerShape(25.dp),
        color = UIWhite,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = UIBlack, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Search", color = UIDarkGrey, fontSize = 16.sp)
        }
    }
}

@Composable
fun SmallCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = UIWhite,
        modifier = modifier
            .size(36.dp)
            .clickable { }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = UIBlack)
        }
    }
}

@Composable
fun FloatingAddButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .size(60.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = UIAccentYellow,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Add,
                null,
                tint = UIBlack,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun EmptyStateMessage(onNavigateToAddActivity: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Oops.",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = UIDarkGrey
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You haven't made any activities.",
            fontSize = 16.sp,
            color = UIDarkGrey
        )
    }
}

@Preview
@Composable
fun DetailedPreview() {
    LucaTheme {
        DetailedEventScreen()
    }
}