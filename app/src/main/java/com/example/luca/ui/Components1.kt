package com.example.luca.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun FloatingNabar(
    // Callback buat ngasih tau page mana yang dipilih (0: Left, 1: Center, 2: Right)
    onItemSelected: (Int) -> Unit = {}
) {
    // State untuk melacak posisi aktif (Default 1 = Tengah/Home)
    var selectedIndex by remember { mutableIntStateOf(1) }

    // Logic Animasi: Menentukan posisi X lingkaran putih berdasarkan index
    // Perhitungan manual berdasarkan width 225.dp dan item 59.dp dengan SpaceEvenly
    // Gap antar item kira-kira 12.dp -> (225 - (59*3)) / 4
    val indicatorOffset by animateDpAsState(
        targetValue = when (selectedIndex) {
            0 -> 12.dp  // Posisi Kiri
            1 -> 83.dp  // Posisi Tengah (12 + 59 + 12)
            2 -> 154.dp // Posisi Kanan (83 + 59 + 12)
            else -> 83.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, // Biar ada mental-mentul dikit
            stiffness = Spring.StiffnessLow
        ),
        label = "IndicatorAnimation"
    )

    Surface(
        modifier = Modifier
            .offset(y = (-23).dp)
            .width(225.dp)
            .height(75.dp),
        shape = RoundedCornerShape(50.dp),
        color = UiAccentYellow,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart // Start alignment penting buat offset bekerja
        ) {

            // --- LAYER 1: Indikator Putih Bergerak ---
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset) // Ini yang bikin dia geser
                    .size(59.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(30.dp))
                    .background(color = UIWhite, shape = RoundedCornerShape(30.dp))
            )

            // --- LAYER 2: Icon Buttons (Foreground) ---
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- Button 1: Scan ---
                NavIconButton(
                    iconRes = R.drawable.ic_scan_button,
                    desc = "Scan",
                    isSelected = selectedIndex == 0,
                    onClick = {
                        selectedIndex = 0
                        onItemSelected(0)
                    }
                )

                // --- Button 2: Center (Home / Plus Logic) ---
                // Logic: Kalau selectedIndex == 1 (lagi di Home), iconnya Plus.
                // Kalau lagi di page lain (0 atau 2), iconnya jadi Home (buat balik).
                val centerIconRes = if (selectedIndex == 1) {
                    R.drawable.ic_plus_button // Pastikan icon ini ada (ganti icon plus)
                } else {
                    R.drawable.ic_home_button
                }

                NavIconButton(
                    iconRes = centerIconRes,
                    desc = "Home/Plus",
                    isSelected = selectedIndex == 1,
                    onClick = {
                        selectedIndex = 1
                        onItemSelected(1)
                    }
                )

                // --- Button 3: Contacts ---
                NavIconButton(
                    iconRes = R.drawable.ic_contacts_button,
                    desc = "Contacts",
                    isSelected = selectedIndex == 2,
                    onClick = {
                        selectedIndex = 2
                        onItemSelected(2)
                    }
                )
            }
        }
    }
}

// Component helper biar kodingan di atas gak berantakan
@Composable
fun NavIconButton(
    iconRes: Int,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(59.dp)
            // InteractionSource null biar ga ada ripple effect pas klik (opsional)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = desc,
            tint = UIBlack // Warna icon selalu hitam sesuai request
        )
    }
}

// Definisikan variasi Header yang ada di gambar
enum class HeaderStat(
    val title: String,
    val showLeftIconAsBack: Boolean, // False = Hamburger, True = Arrow Back
    val showRightLogo: Boolean
) {
    HOME("Luca", false, true),       // State 1: Hamburger + Logo
    NEW_EVENT("New Event", true, false), // State 2: Back + No Logo
    NEW_ACTIVITY("New Activity", true, false),
    DETAILS("Activity Details", true, false),
    SUMMARY("Summary", true, false)
}

@Composable
fun HeaderSectio(
    // Parameter utama: State saat ini
    currentState: HeaderState = HeaderState.HOME,

    // Callback buat navigasi beneran
    onLeftIconClick: () -> Unit = {},

    // Callback buat testing (bisa dihapus/diksongin nanti)
    onTitleDebugClick: () -> Unit = {}
) {
    Surface(
        color = UIWhite,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(60.dp) // Sedikit digedein biar lega
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
                .background(UIWhite),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // --- 1. LEFT ICON ANIMATION ---
            // Crossfade: Transisi halus antara Hamburger dan Back Arrow
            Box(
                modifier = Modifier.fillMaxHeight().width(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(
                    targetState = currentState.showLeftIconAsBack,
                    label = "LeftIconAnim"
                ) { isBack ->
                    val iconRes = if (isBack) R.drawable.ic_arrow_back else R.drawable.ic_hamburger_sidebar
                    val desc = if (isBack) "Back" else "Menu"

                    IconButton(onClick = onLeftIconClick) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = desc,
                            tint = UIBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --- 2. TITLE ANIMATION & DEBUG TRIGGER ---
            // AnimatedContent: Teks lama geser ke atas, teks baru masuk dari bawah
            AnimatedContent(
                targetState = currentState.title,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                },
                label = "TitleAnim"
            ) { titleText ->
                Text(
                    text = titleText,
                    // style = AppFont.SemiBold, // Un-comment kalo font udah ada
                    color = UIBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier
                        // --- AREA DEBUG ---
                        // Nanti kalau udah final, hapus .clickable ini atau kosongin isinya
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Biar ga ada ripple effect pas diklik
                        ) {
                            onTitleDebugClick()
                        }
                    // ------------------
                )
            }

            // --- 3. RIGHT ICON ANIMATION ---
            // AnimatedVisibility: Logo cuma muncul di Home
            Box(
                modifier = Modifier.fillMaxHeight().width(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                    AnimatedVisibility(
                        visible = currentState.showRightLogo,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        // Ganti icon logo cookie/luca kamu disini
                        Image(
                            painter = painterResource(id = R.drawable.ic_luca_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TestHeaderScreen() {
    // Variable lokal buat nampung state (Cuma buat testing)
    var currentHeaderState by remember { mutableStateOf(HeaderState.HOME) }

    Column(modifier = Modifier.fillMaxSize().background(UIGrey)) {

        HeaderSection(
            currentState = currentHeaderState,
            onLeftIconClick = {
                println("Left icon clicked!")
            },
            onTitleDebugClick = {
                // LOGIC SHUFFLE: Pindah ke enum berikutnya
                val nextOrdinal = (currentHeaderState.ordinal + 1) % HeaderState.values().size
                currentHeaderState = HeaderState.values()[nextOrdinal]
            }
        )

        // Konten dummy di bawahnya
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Konten Halaman: ${currentHeaderState.title}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestNavbarPreview() {
    // Pakai Box buat simulasi layar HP
    TestHeaderScreen()
}
