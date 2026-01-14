package com.example.luca

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

enum class HeaderState(
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
fun HeaderSection(
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
                    androidx.compose.animation.AnimatedVisibility(
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
fun FloatingNavbar(
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
        color = UIAccentYellow,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputSection(
    label: String,
    value: String,
    placeholder: String,
    testTag: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = AppFont.SemiBold,
            fontSize = 16.sp,
            color = UIBlack,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, color = UIDarkGrey)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag(testTag),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = UIWhite,
                unfocusedContainerColor = UIWhite,
                disabledContainerColor = UIWhite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = UIBlack,
                unfocusedTextColor = UIBlack
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
    }
}

@Composable
fun ParticipantItem(
    name: String,
    isAddButton: Boolean = false,
    isYou: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp) // Ukuran lingkaran luar (Container)
                .clip(CircleShape)
                .background(UIGrey)
                .testTag(if (isAddButton) "btn_add_participant" else "avatar_$name"),
            contentAlignment = Alignment.Center
        ) {
            if (isAddButton) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_button),
                    contentDescription = "Add Participant",
                    tint = UIBlack,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (!isAddButton) {
            Text(
                text = name,
                fontSize = 12.sp,
                style = if (isYou) AppFont.SemiBold else AppFont.Regular,
                maxLines = 1,
                color = UIBlack
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(220.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = text,
            color = UIBlack,
            style = AppFont.SemiBold,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun StackedAvatarRow(
    spacing: Int = -10,
    avatars: List<String>,
    maxVisible: Int = 4,
    itemSize: Dp = 40.dp // Tambahan: Ukuran standar untuk avatar & counter
) {
    // 1. Hitung Logic Sisa
    val isOverflow = avatars.size > maxVisible

    // Jika overflow, kurangi 1 slot untuk tempat counter (+N)
    val visibleCount = if (isOverflow) maxVisible - 1 else avatars.size
    val remainingCount = avatars.size - visibleCount

    Row(
        horizontalArrangement = Arrangement.spacedBy((spacing).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2. Render Foto (Looping sebanyak visibleCount)
        for (i in 0 until visibleCount) {
            // Kita bungkus AvatarItem biar bisa dikontrol zIndex-nya dari sini jika perlu
            // Atau asumsikan AvatarItem sudah handle size
            Box(
                modifier = Modifier
                    .zIndex((visibleCount - i).toFloat()) // Biar yang kiri selalu di atas (tumpukan menurun ke kanan)
            ) {
                AvatarIte(
                    imageCode = avatars[i],
                    size = itemSize,
                    zIndex = (visibleCount - i).toFloat()
                )
            }
        }

        // 3. Render Counter Overflow (Jika ada sisa)
        if (isOverflow) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .zIndex(0f) // Paling bawah tumpukannya
                    .size(itemSize)
                    .clip(CircleShape)
                    .background(UIDarkGrey)
                    // Optional: Kasih border putih biar misah sama foto sebelumnya
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = "+$remainingCount",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = AppFont.Bold
                )
            }
        }
    }
}

// Komponen Dummy untuk AvatarItem (Hanya sebagai contoh agar kode di atas tidak error)
@Composable
fun AvatarItem(imageCode: String, zIndex: Float, size: Dp = 40.dp) {
    val commonModifier = Modifier
        .size(size)
        .zIndex(zIndex)
        .clip(CircleShape)
        .border(2.dp, UIWhite, CircleShape)

    if (imageCode == "debug") {
        Box(modifier = commonModifier.background(Color.Gray))
    } else {
        // Ambil ID resource berdasarkan kode string/angka
        val imageRes = getResourceId(imageCode)

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = commonModifier.background(UIWhite)
        )
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun getResourceId(name: String): Int {
    val context = androidx.compose.ui.platform.LocalContext.current
    return context.resources.getIdentifier(
        "avatar_$name", // Nama file tanpa ekstensi
        "drawable",
        context.packageName
    )
}

@Preview
@Composable
fun ComponentsPreview(){
    LucaTheme {
        // Preview dummy
        Column(modifier = Modifier.background(UIWhite).padding(16.dp)) {
            PrimaryButton(text = "Test Button", onClick = {})
        }
    }
}