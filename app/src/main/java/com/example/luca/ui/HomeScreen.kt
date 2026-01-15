package com.example.luca.ui

import com.example.luca.R
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.*
import com.example.luca.model.Event
import com.example.luca.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    // 1. DATA EVENT
    val allEvents = remember {
        listOf(
            Event("1", "Bali With The Boys", "Buleleng, Bali", "Aug 24, 2025", "", emptyList()),
            Event("2", "Dinner at PIK", "Jakarta Utara", "Sep 10, 2025", "", emptyList()),
            Event("3", "Hiking Gunung Gede", "Cianjur, Jawa Barat", "Oct 05, 2025", "", emptyList())
        )
    }

    // 2. STATE
    var searchQuery by remember { mutableStateOf("") }

    // PENTING: State scroll didefinisikan di sini biar bisa dikontrol search bar
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 3. LOGIKA AUTO-SCROLL SAAT KETIK
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            // Cari index event pertama yang judulnya mengandung text search
            val index = allEvents.indexOfFirst {
                it.title.contains(searchQuery, ignoreCase = true)
            }

            if (index != -1) {
                // KETEMU: Scroll ke kartu tersebut
                listState.animateScrollToItem(index)
            }
        }
    }

    Scaffold(
        topBar = { HeaderSection() },
        floatingActionButton = { FloatingNavbar() },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIAccentYellow)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- SEARCH BAR AREA ---
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 25.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BetterSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }

                // --- CONTENT AREA ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(UIBackground)
                ) {

                    if (allEvents.isEmpty()) {
                        EmptyStateView()
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Kirim listState ke Carousel
                            EventCarousel(events = allEvents, listState = listState)
                        }
                        Spacer(modifier = Modifier.height(112.dp))
                    }
                }
            }
        }
    }
}

// --- COMPONENT: SEARCH BAR YANG DIPERBAIKI (Teks gak kepotong) ---
@Composable
fun BetterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    // Menggunakan BasicTextField agar lebih mudah diatur ukurannya
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = TextStyle(
            color = UIBlack, // Pastikan warna teks HITAM
            fontSize = 16.sp,
            fontFamily = AppFont.Regular.fontFamily
        ),
        singleLine = true,
        cursorBrush = SolidColor(UIBlack),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(UIWhite)
                    .padding(horizontal = 16.dp), // Padding kiri kanan dalam kotak
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ICON SEARCH
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_logo),
                    contentDescription = "Search",
                    tint = UIBlack,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // AREA TEKS
                Box(modifier = Modifier.weight(1f)) {
                    // Placeholder (Muncul kalau kosong)
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            style = AppFont.Regular,
                            color = UIDarkGrey,
                            fontSize = 16.sp
                        )
                    }
                    // Tempat ngetik
                    innerTextField()
                }
            }
        }
    )
}

// --- COMPONENT: CAROUSEL DENGAN STATE DARI LUAR ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCarousel(
    events: List<Event>,
    listState: LazyListState // Menerima state dari atas
) {
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = 280.dp
    val cardSpacing = 20.dp
    val sidePadding = (screenWidth - cardWidth) / 2

    LazyRow(
        state = listState, // Pakai state yang dikirim
        flingBehavior = snapBehavior,
        contentPadding = PaddingValues(horizontal = sidePadding),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Pakai itemsIndexed jika butuh index, tapi items biasa juga oke
        itemsIndexed(events) { index, event ->
            EventCard(
                event = event,
                width = cardWidth,
                onClick = { /* TODO: Navigate to Detail */ }
            )
        }
    }
}

// --- EmptyStateView (Tetap sama) ---
@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Oops.", style = AppFont.Bold, fontSize = 24.sp, color = UIDarkGrey)
        Text(
            "You haven't made any events.\nClick here to add one!",
            style = AppFont.Regular, fontSize = 16.sp, color = UIDarkGrey, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            modifier = Modifier.size(64.dp), shape = CircleShape, color = UIAccentYellow, shadowElevation = 4.dp,
            onClick = { }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(id = R.drawable.ic_plus_button), "Add", tint = UIBlack, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    LucaTheme {
        HomeScreen()
    }
}