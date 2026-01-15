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
import com.example.luca.*
import com.example.luca.model.Event
import com.example.luca.ui.theme.*
import com.example.luca.viewmodel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    // 1. Inject ViewModel agar terhubung ke Firebase
    viewModel: HomeViewModel = viewModel(),
    // 2. Callback navigasi (Nanti dipake di MainActivity)
    onNavigateToDetail: (String) -> Unit = {}
) {
    // OBSERVE DATA (Mata-matai ViewModel)
    // state 'allEvents' diperlukan untuk logic scroll otomatis
    val allEvents by viewModel.events.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Ambil list yang sudah difilter berdasarkan search
    val filteredEvents = viewModel.getFilteredEvents()

    // State untuk List & Scroll
    val listState = rememberLazyListState()

    // LOGIKA AUTO-SCROLL (Dipindah ke sini, memantau data asli)
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

fun HomeScreen(
    onEventClick: () -> Unit = {},
    onContactsClick: () -> Unit = {},
    onAddEventClick: () -> Unit = {}
) {
    Scaffold(
        // 1. Header Tetap di Atas
        topBar = {
            HeaderSection() // Fungsi Header UiDark kamu
        },

        // 2. Navbar Melayang ditaruh di slot FAB agar tidak memotong list
        floatingActionButton = {
            FloatingNavbar(
                onContactsClick = onContactsClick,
                onAddClick = onAddEventClick
            )
        },
        // Atur posisi Navbar ke tengah bawah
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
                        onQueryChange = {
                            // Panggil fungsi di ViewModel
                            viewModel.onSearchQueryChanged(it)
                        }
                    )
                }

                // --- CONTENT AREA ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(UIBackground)
                ) {

                    if (filteredEvents.isEmpty()) {
                        // Tampilkan Empty State jika data kosong/belum loading
                        EmptyStateView()
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Kirim data yang sudah difilter ke Carousel
                            EventCarousel(
                                events = filteredEvents,
                                listState = listState,
                                onEventClick = onNavigateToDetail
                            )
                        }
                        // Spacer untuk memberi ruang pada FAB
                        Spacer(modifier = Modifier.height(112.dp))
                ){
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EventCarousel(onEventClick = onEventClick)
                    }
                }
            }
        }
    }
}

// --- COMPONENT: SEARCH BAR ---
@Composable
fun EventCarousel(
    onEventClick: () -> Unit = {}
) {
    // 1. Setup State buat scroll dan snap
    val listState = rememberLazyListState()
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
            // Pastikan kamu punya komponen EventCard di Components.kt
            // Jika belum ada, pastikan importnya benar atau copy komponen EventCard ke sini
            EventCard(
                event = event,
                width = cardWidth,
                onClick = { onEventClick(event.id) } // Kirim ID saat diklik
            )
        // Contoh 5 Item dummy
        items(5) { index ->
            TripCardItem(width = cardWidth, index = index, onClick = onEventClick)
        }
    }
}

// --- COMPONENT: EMPTY STATE ---
@Composable
fun TripCardItem(width: Dp, index: Int, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(width)
            .height(400.dp)
            .shadow(elevation = 8.dp,
                shape = RoundedCornerShape(25.dp),
                clip = false
            ),
        shape = RoundedCornerShape(25.dp),
        colors = CardColors(
            containerColor = UIWhite,
            contentColor = UIWhite,
            disabledContainerColor = UIWhite,
            disabledContentColor = UIWhite
        ),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(all = 15.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(15.dp))
                            .background(UIDarkGrey),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Text(text = "nanti ini isinya foto",
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            textAlign = TextAlign.Center)

                        Box(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            StackedAvatarRow(itemSize = 40.dp, avatars = listOf("debug", "debug", "debug", "debug", "debug"))
                        }
                    }
                }

                Text(text = "Bali With The Boys",
                    color = UIBlack,
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp),
                    style = AppFont.SemiBold,
                    fontSize = 20.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(R.drawable.ic_location_marker),
                        contentDescription = "Location Icon",
                        tint = UIAccentYellow)

                    Spacer(modifier = Modifier.fillMaxHeight().width(5.dp))

                    Text(text = "Buleleng, Bali, Indonesia",
                        color = UIDarkGrey,
                        style = AppFont.Medium,
                        fontSize = 16.sp)
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(text = "August 24, 2025",
                        color = UIBlack,
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun TempSearchBar() {
    Box(
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
            onClick = { /* TODO: Navigate to Add Event */ }
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
        // Preview dengan ViewModel kosong/default
        HomeScreen()
    }
}