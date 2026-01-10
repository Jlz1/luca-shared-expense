package com.example.luca

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentRed
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBackground
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun HomeScreen() {
    Scaffold(
        // 1. Header Tetap di Atas
        topBar = {
            HeaderSection() // Fungsi Header UiDark kamu
        },

        // 2. Navbar Melayang ditaruh di slot FAB agar tidak memotong list
        floatingActionButton = {
            FloatingNavbar()
        },
        // Atur posisi Navbar ke tengah bawah
        floatingActionButtonPosition = FabPosition.Center,

        // Memastikan konten bisa 'tembus' ke area bawah layar
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // Tambahkan padding ini agar konten mulai di bawah Header
        Box(modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 25.dp),
                    contentAlignment = Alignment.Center
                ){
                    TempSearchBar()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(
                            topStart = 30.dp,
                            topEnd = 30.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        ))
                        .background(UIBackground)
                ){
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EventCarousel()
                    }
                    Spacer(modifier = Modifier.fillMaxWidth().height(112.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class) // Snapping masih experimental api di beberapa versi, aman dipake
@Composable
fun EventCarousel() {
    // 1. Setup State buat scroll dan snap
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 2. Hitung ukuran biar Card pas di tengah
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
        // Contoh 5 Item dummy
        items(5) { index ->
            TripCardItem(width = cardWidth, index = index)
        }
    }
}

@Composable
fun TripCardItem(width: Dp, index: Int) {
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
        )
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
                            StackedAvatarRow(List(4, {"a"}))
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
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(20.dp))
            .background(UIWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_search_logo),
                contentDescription = "Search Icon",
                tint = UIBlack)

            Spacer(modifier = Modifier.fillMaxHeight().width(16.dp))

            Text(
                text = "Search",
                style = AppFont.Regular,
                fontSize = 16.sp,
                color = UIDarkGrey
            )
        }
    }
}

@Composable
fun StackedAvatarRow(
    avatars: List<String>, // List URL gambar
    maxVisible: Int = 4    // Maksimal bunderan yang muncul (termasuk counter)
) {
    // 1. Hitung Logic Sisa
    // Kalau jumlah total > maxVisible, kita cuma tampilin (max - 1) foto, sisanya buat counter
    val isOverflow = avatars.size > maxVisible
    val visibleCount = if (isOverflow) maxVisible - 1 else avatars.size
    val remainingCount = avatars.size - visibleCount

    Row(
        // 2. Bikin efek numpuk (Spacing Negatif)
        horizontalArrangement = Arrangement.spacedBy((-10).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render Foto
        for (i in 0 until visibleCount) {
            AvatarItem(
                imageUrl = avatars[i],
                zIndex = (visibleCount - i).toFloat()
            )
        }
    }
}

// Komponen Foto Bulat
@Composable
fun AvatarItem(imageUrl: String, zIndex: Float) {
    Box(
        modifier = Modifier
            .size(30.dp) // Ukuran Lingkaran
            .zIndex(zIndex) // <--- PENTING BUAT TUMPUKAN
            .clip(CircleShape)
            .border(2.dp, UIWhite, CircleShape) // Border putih pemisah
            .background(UIAccentRed) // Placeholder bg
    ) {

    }
}

@Preview
@Composable
fun Kevin() {
    LucaTheme {
        HomeScreen()
    }
}