package com.example.luca.ui

import com.example.luca.R
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luca.model.Event
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.HomeViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.luca.data.LucaFirebaseRepository

// --- STATEFUL COMPOSABLE (Logic) ---
@Composable
fun HomeScreen(
    // 1. UPDATE DI SINI: Kita inject Repository ke ViewModel menggunakan Factory
    viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(repository = LucaFirebaseRepository())
            }
        }
    ),
    onNavigateToDetail: (String) -> Unit = {},
    onContactsClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {}
) {
    val allEvents by viewModel.events.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Logic filter dipanggil setiap kali UI direcompose
    val filteredEvents = viewModel.getFilteredEvents()

    val listState = rememberLazyListState()

    // Opsional: Refresh data setiap kali user kembali ke halaman ini
    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    // Auto-scroll logic (Tetap sama)
    LaunchedEffect(searchQuery, allEvents) {
        if (searchQuery.isNotEmpty() && allEvents.isNotEmpty()) {
            val index = allEvents.indexOfFirst {
                it.title.contains(searchQuery, ignoreCase = true)
            }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    HomeScreenContent(
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
        filteredEvents = filteredEvents, // Ini sekarang berisi data real dari Firebase
        listState = listState,
        onEventClick = onNavigateToDetail,
        onAddEventClick = onAddEventClick
    )
}

// --- STATELESS COMPOSABLE (UI Only) ---
@Composable
fun HomeScreenContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredEvents: List<Event>,
    listState: LazyListState,
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit
) {
    // 1. HAPUS SCAFFOLD. Ganti langsung dengan Container utama.
    // Kita pakai Column full screen dengan background kuning.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            // Tambahkan status bar padding jika perlu, atau biarkan MainActivity yang handle
            .statusBarsPadding()
    ) {

        // 2. HEADER SECTION (Dulunya di topBar Scaffold, sekarang ditaruh manual disini)
        // Pastikan komponen HeaderSection() kamu ada dan bisa diakses
        HeaderSection()

        // 3. SEARCH BAR AREA
        Box(
            modifier = Modifier
                .height(90.dp) // Sesuaikan tinggi area search
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 25.dp),
            contentAlignment = Alignment.Center
        ) {
            BetterSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )
        }

        // 4. WHITE CONTENT AREA (Carousel / Empty State)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(UIBackground)
        ) {
            if (filteredEvents.isEmpty()) {
                // Empty state butuh tombol add, jadi kita passing fungsinya
                EmptyStateView(onAddEventClick = onAddEventClick)
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EventCarousel(
                        events = filteredEvents,
                        listState = listState,
                        onEventClick = onEventClick
                    )
                }
            }

            // Spacer di bawah agar konten paling bawah tidak ketutup Navbar MainActivity
            // Navbar tingginya sekitar 80dp-100dp, jadi kita kasih spacer aman.
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// --- COMPONENT: SEARCH BAR ---
@Composable
fun BetterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = TextStyle(
            color = UIBlack,
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
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_logo),
                    contentDescription = "Search",
                    tint = UIBlack,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            style = AppFont.Regular,
                            color = UIDarkGrey,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

// --- COMPONENT: CAROUSEL ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCarousel(
    events: List<Event>,
    listState: LazyListState,
    onEventClick: (String) -> Unit
) {
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = 280.dp
    val cardSpacing = 20.dp
    val sidePadding = (screenWidth - cardWidth) / 2

    LazyRow(
        state = listState,
        flingBehavior = snapBehavior,
        contentPadding = PaddingValues(horizontal = sidePadding),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(events) { index, event ->
            EventCard(
                event = event,
                width = cardWidth,
                onClick = { onEventClick(event.id) }
            )
        }
    }
}

// --- COMPONENT: EMPTY STATE ---
@Composable
fun EmptyStateView(
    onAddEventClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops.",
            style = AppFont.Bold,
            fontSize = 24.sp,
            color = UIDarkGrey
        )
        Text(
            text = "You haven't made any events.\nClick here to add one!",
            style = AppFont.Regular,
            fontSize = 16.sp,
            color = UIDarkGrey,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = UIAccentYellow,
            shadowElevation = 4.dp,
            onClick = onAddEventClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus_button),
                    contentDescription = "Add",
                    tint = UIBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- PREVIEW ---
@Preview
@Composable
fun HomeScreenPreview() {
    LucaTheme {
        HomeScreenContent(
            searchQuery = "",
            onSearchQueryChange = {},
            filteredEvents = emptyList(),
            listState = rememberLazyListState(),
            onEventClick = {},
            onAddEventClick = {}
        )
    }
}