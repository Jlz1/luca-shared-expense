package com.example.luca.ui

import android.annotation.SuppressLint
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.luca.R
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.components.AvatarSelectionOverlay
import com.example.luca.util.AvatarUtils
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite
import com.example.luca.model.Event
import java.text.NumberFormat
import java.util.Locale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.luca.model.BankAccountData
import com.example.luca.model.Contact
import com.example.luca.util.BankUtils
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.verticalScroll

// Header State Definition
enum class HeaderState(
    val title: String,
    val showLeftIconAsBack: Boolean, // False = Hamburger, True = Arrow Back
    val showRightLogo: Boolean
) {
    HOME("Luca", false, true),       // State 1: Hamburger + Logo
    NEW_EVENT("New Event", true, false), // State 2: Back + No Logo
    NEW_ACTIVITY("New Activity", true, false),
    DETAILS("Activity Details", true, false),
    EDIT_ACTIVITY("Edit Activity", true, false)
}

// Header Section
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
                    style = AppFont.Bold,
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
                    this@Row.AnimatedVisibility(
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
    // 1. INI PARAMETER BARUNYA (State dari luar)
    // 0 = Kiri (Scan), 1 = Tengah (Home), 2 = Kanan (Contacts)
    selectedIndex: Int = 1,

    // 2. Callback buat lapor ke Parent kalo user klik tombol
    onItemSelected: (Int) -> Unit,

    // 3. Callback spesifik
    onContactsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    // Logic Animasi: Sekarang gerak berdasarkan parameter 'selectedIndex'
    val indicatorOffset by animateDpAsState(
        targetValue = when (selectedIndex) {
            0 -> 12.dp
            1 -> 83.dp
            2 -> 154.dp
            else -> 83.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "IndicatorAnimation"
    )

    Surface(
        modifier = Modifier
            .offset(y = (-23).dp) // Offset ke atas biar nangkring
            .width(225.dp)
            .height(75.dp),
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFFFFC107), // UIAccentYellow
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {

            // LAYER 1: Indikator Putih Bergerak
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset) // Posisi ikut parameter
                    .size(59.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(30.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(30.dp))
            )

            // LAYER 2: Icon Buttons
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- Button 1: Scan ---
                NavIconButton(
                    iconRes = R.drawable.ic_scan_button, // Ganti resource iconmu
                    desc = "Scan",
                    // Cek apakah tab ini yang dipilih berdasarkan parameter
                    isSelected = selectedIndex == 0,
                    onClick = {
                        onItemSelected(0) // Lapor parent: "User mau ke tab 0"
                    }
                )

                // --- Button 2: Center (Home / Plus Logic) ---
                // Kalau parameter == 1 (lagi di Home), icon jadi Plus
                val centerIconRes = if (selectedIndex == 1) {
                    R.drawable.ic_plus_button
                } else {
                    R.drawable.ic_home_button
                }

                NavIconButton(
                    iconRes = centerIconRes,
                    desc = "Home/Plus",
                    isSelected = selectedIndex == 1,
                    onClick = {
                        if (selectedIndex == 1) {
                            // Kalau udah di home, klik lagi -> Add Event
                            onAddClick()
                        } else {
                            // Kalau dari tab lain, klik ini -> Balik Home
                            onItemSelected(1)
                            onHomeClick()
                        }
                    }
                )

                // --- Button 3: Contacts ---
                NavIconButton(
                    iconRes = R.drawable.ic_contacts_button,
                    desc = "Contacts",
                    isSelected = selectedIndex == 2,
                    onClick = {
                        onItemSelected(2) // Lapor parent: "User mau ke tab 2"
                        onContactsClick()
                    }
                )
            }
        }
    }
}

// Helper For Navbar Button
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

// Input Section
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

// Add Participant Item
@Suppress("unused")
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

// Simple Button Template
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

// --- UPDATE INSIDE StackedAvatarRow ---
@Composable
fun StackedAvatarRow(
    spacing: Int = -10,
    avatars: List<String>,
    maxVisible: Int = 4,
    itemSize: Dp = 40.dp
) {
    // ... existing logic for overflow ...
    val isOverflow = avatars.size > maxVisible
    val visibleCount = if (isOverflow) maxVisible - 1 else avatars.size
    // val remainingCount = avatars.size - visibleCount // TODO: Use for overflow count display

    Row(
        horizontalArrangement = Arrangement.spacedBy((spacing).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2. Render Foto (Looping)
        for (i in 0 until visibleCount) {
            Box(
                modifier = Modifier.zIndex((visibleCount - i).toFloat())
            ) {
                // UPDATE THIS CALL:
                AvatarItem(
                    imageUrl = avatars[i], // Pass the URL string here
                    size = itemSize,
                    zIndex = (visibleCount - i).toFloat()
                )
            }
        }

        // ... existing logic for counter ...
        if (isOverflow) {
            // TODO: Show overflow indicator (+N more)
        }
    }
}

@Composable
fun AvatarItem(
    imageUrl: String,
    zIndex: Float = 0f, // Default value biar aman
    size: Dp = 40.dp
) {
    val context = LocalContext.current

    val commonModifier = Modifier
        .size(size)
        .zIndex(zIndex)
        .clip(CircleShape)
        .border(2.dp, UIWhite, CircleShape)
        .background(UIGrey) // <--- INI KUNCINYA: Warna abu saat loading

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true) // Animasi halus saat gambar muncul
            .diskCachePolicy(CachePolicy.ENABLED) // Simpan di memori HP
            .memoryCachePolicy(CachePolicy.ENABLED) // Simpan di RAM
            .build(),
        contentDescription = "Avatar",
        modifier = commonModifier,
        contentScale = ContentScale.Crop,
        // JANGAN PAKAI PLACEHOLDER GAMBAR (Biar cuma warna abu yg kelihatan)
        placeholder = null,
        // Kalau error (link mati/gak ada internet), baru munculkan icon default/android
        error = painterResource(id = R.drawable.ic_launcher_foreground)
    )
}

// Event Card Layout
@Composable
fun EventCard(
    event: Event,
    width: Dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(width)
            .height(400.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(25.dp), clip = false)
            .clickable { onClick() },
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = UIWhite
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(all = 15.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- 1. GAMBAR EVENT (UPDATED) ---
                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(15.dp))
                            .background(UIGrey), // <--- Warna abu saat loading (ganti UIDarkGrey jadi UIGrey biar lebih soft)
                        contentAlignment = Alignment.Center // Default center biar icon error di tengah
                    ) {
                        // LOGIC BARU: Tampilkan gambar dari URL dengan Caching Agresif
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(event.imageUrl)
                                .crossfade(true) // Fade in halus
                                .crossfade(400) // Durasi 0.4 detik
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Event Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = null, // Biarkan background abu yang bekerja
                            error = painterResource(id = R.drawable.ic_launcher_foreground) // Icon android cuma muncul kalau ERROR/GAGAL
                        )

                        // Avatar Stack (Tetap menumpuk di atas gambar)
                        Box(modifier = Modifier.padding(10.dp)) {
                            StackedAvatarRow(
                                itemSize = 36.dp,
                                avatars = event.participantAvatars
                            )
                        }
                    }
                }

                // --- 2. JUDUL (TETAP SAMA) ---
                Text(
                    text = event.title,
                    color = UIBlack,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    style = AppFont.SemiBold,
                    fontSize = 20.sp,
                    maxLines = 1
                )

                // --- 3. LOKASI (TETAP SAMA) ---
                Row(
                    modifier = Modifier.fillMaxWidth().height(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location_marker),
                        contentDescription = "Location",
                        tint = UIAccentYellow
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = event.location,
                        color = UIDarkGrey,
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                // --- 4. TANGGAL (TETAP SAMA) ---
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = event.date,
                        color = UIBlack,
                        style = AppFont.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Helper untuk mendapatkan ID drawable avatar berdasarkan nama
@Suppress("unused")
@SuppressLint("LocalContextResourcesRead")
@Composable
fun getResourceId(name: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(
        "avatar_$name", // Nama file tanpa ekstensi
        "drawable",
        context.packageName
    )
}

@Composable
fun UserProfileOverlay(
    onClose: () -> Unit,
    // Callback mengirim data: Nama, HP, List Bank, Avatar (removed description)
    onAddContact: (String, String, List<BankAccountData>, String) -> Unit,
    // Add optional parameter for editing existing contact
    editContact: Contact? = null,
    onUpdateContact: ((String, String, String, String, List<BankAccountData>, String) -> Unit)? = null
) {
    // State Input - Pre-fill if editing
    var name by remember { mutableStateOf(editContact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(editContact?.phoneNumber ?: "") }
    var description by remember { mutableStateOf(editContact?.description ?: "") }

    // State Bank & Dialog - Pre-fill if editing
    var bankAccounts by remember { mutableStateOf<List<BankAccountData>>(editContact?.bankAccounts ?: emptyList()) }
    var showBankDialog by remember { mutableStateOf(false) }
    var bankFullError by remember { mutableStateOf(false) }

    // Dialog state variables
    var selectedBank by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }

    // State Avatar - Pre-fill if editing
    var showAvatarDialog by remember { mutableStateOf(false) }
    var selectedAvatarName by remember { mutableStateOf(editContact?.avatarName ?: "") }

    // State Validation Error
    var showNameError by remember { mutableStateOf(false) }

    // Auto-hide bank full error after 3 seconds
    LaunchedEffect(bankFullError) {
        if (bankFullError) {
            delay(3000) // 3 seconds
            bankFullError = false
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight() // Fix error wrapContentHeight
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()) // Fix error verticalScroll
        ) {
            // --- HEADER ---
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(32.dp), tint = UIBlack)
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { showAvatarDialog = true }
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedAvatarName.isNotEmpty()) {
                        // Show selected avatar without camera overlay
                        Image(
                            painter = painterResource(id = AvatarUtils.getAvatarResId(selectedAvatarName)),
                            contentDescription = "Selected Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Show camera icon with grey background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(UIGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                "Select Avatar",
                                tint = UIDarkGrey,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                // TOMBOL SAVE (CHECK)
                IconButton(
                    onClick = {
                        // Validasi: Nama harus diisi
                        if (name.isBlank()) {
                            showNameError = true
                        } else {
                            // Check if editing or adding
                            if (editContact != null && onUpdateContact != null) {
                                // Update existing contact
                                onUpdateContact(editContact.id, name, phoneNumber, description, bankAccounts, selectedAvatarName)
                            } else {
                                // Add new contact
                                onAddContact(name, phoneNumber, bankAccounts, selectedAvatarName)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Check, "Save", modifier = Modifier.size(32.dp), tint = UIBlack)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // INPUT FIELDS
            CustomRoundedTextField(value = name, onValueChange = { name = it; showNameError = false }, placeholder = "Name", backgroundColor = UIGrey)

            // Error Message for Name
            if (showNameError) {
                Text(
                    text = "Nama contact harus diisi",
                    color = Color.Red,
                    fontSize = 12.sp,
                    style = AppFont.Regular,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            CustomRoundedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, placeholder = "Phone Number (Optional)", backgroundColor = UIGrey)

            Spacer(modifier = Modifier.height(24.dp))

            // BANK ACCOUNTS SECTION
            Text("Bank Accounts", fontSize = 18.sp, style = AppFont.Bold, color = UIBlack)
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (bankAccounts.size >= 3) UIGrey else UIAccentYellow)
                    .clickable(enabled = bankAccounts.size < 3) {
                        if (bankAccounts.size < 3) {
                            showBankDialog = true
                        } else {
                            bankFullError = true
                        }
                    }
            ) {
                Icon(Icons.Default.Add, "Add Bank", tint = Color.Black, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LIST BANK ITEMS
            if (bankAccounts.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    bankAccounts.forEach { account ->
                        // Panggil Component BankAccountItem (Pastikan kode di bawah dicopy juga)
                        BankAccountItem(
                            bankName = account.bankName,
                            accountNumber = account.accountNumber,
                            bankLogoName = account.bankLogo,
                            onDelete = { bankAccounts = bankAccounts - account }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Error Message for Bank Account Full
            if (bankFullError) {
                Text(
                    text = "Bank account is full",
                    color = Color.Red,
                    fontSize = 12.sp,
                    style = AppFont.Regular,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp)
                )
            }
        }
    }

    // --- DIALOG PILIH BANK ---
    if (showBankDialog) {
        // Fix error "No value passed for parameter bankList":
        // Kita HAPUS parameter bankList dari panggilan ini, karena sudah ada di dalam fungsi BankDialogOverlay
        BankDialogOverlay(
            selectedBank = selectedBank,
            onBankSelected = { selectedBank = it },
            accountNumber = accountNumber,
            onAccountNumberChanged = { accountNumber = it },
            onDismiss = {
                showBankDialog = false
                selectedBank = ""
                accountNumber = ""
                bankFullError = false
            },
            onAdd = {
                if (selectedBank.isNotEmpty() && accountNumber.isNotEmpty()) {
                    // Check if bank account limit (max 3) has been reached
                    if (bankAccounts.size >= 3) {
                        bankFullError = true
                    } else {
                        val logoName = BankUtils.generateLogoFileName(selectedBank)
                        val newBank = BankAccountData(selectedBank, accountNumber, logoName)
                        bankAccounts = bankAccounts + newBank

                        showBankDialog = false
                        selectedBank = ""
                        accountNumber = ""
                    }
                }
            },
            bankAccountCount = bankAccounts.size,
            bankFullError = bankFullError
        )
    }

    // --- DIALOG PILIH AVATAR ---
    if (showAvatarDialog) {
        AvatarSelectionOverlay(
            currentSelection = selectedAvatarName,
            onAvatarSelected = { selectedAvatarName = it },
            onDismiss = { showAvatarDialog = false }
        )
    }
}

@Composable
fun BankDialogOverlay(
    // HAPUS parameter bankList dari sini, kita ambil dari Utils di dalam
    selectedBank: String,
    onBankSelected: (String) -> Unit,
    accountNumber: String,
    onAccountNumberChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    bankAccountCount: Int = 0,
    bankFullError: Boolean = false
) {
    // Ambil list bank langsung dari Utils
    val bankList = BankUtils.availableBanks
    val isAtMaxCapacity = bankAccountCount >= 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UIWhite),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add Bank Account", fontSize = 18.sp, color = UIBlack, modifier = Modifier.padding(bottom = 16.dp))

                // Error Message for Bank Account Full
                if (bankFullError) {
                    Text(
                        text = "Bank account is full",
                        color = Color.Red,
                        fontSize = 12.sp,
                        style = AppFont.Regular,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Scrollable Bank Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bankList.forEach { bank ->
                        Button(
                            onClick = { onBankSelected(bank) },
                            colors = ButtonDefaults.buttonColors(containerColor = if(selectedBank == bank) UIAccentYellow else UIGrey),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            enabled = !isAtMaxCapacity
                        ) {
                            Text(bank, color = if(selectedBank == bank) UIBlack else UIDarkGrey)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                CustomRoundedTextField(value = accountNumber, onValueChange = onAccountNumberChanged, placeholder = "Account Number", backgroundColor = UIGrey, enabled = !isAtMaxCapacity)
                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = UIGrey), modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.Black)
                    }
                    Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = if(isAtMaxCapacity) UIGrey else UIAccentYellow), modifier = Modifier.weight(1f), enabled = !isAtMaxCapacity) {
                        Text("Add", color = Color.Black)
                    }
                }
            }
        }
    }
}

// Komponen untuk button pilihan bank
@Suppress("unused")
@Composable
fun BankButton(
    bankName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) UIAccentYellow else UIGrey
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(4.dp)
            .height(40.dp)
    ) {
        Text(
            text = bankName,
            color = if (isSelected) Color.Black else UIDarkGrey,
            style = AppFont.Medium,
            fontSize = 12.sp
        )
    }
}

// Komponen Helper supaya Text Field nya rounded banget dan clean (tanpa garis bawah)
@Composable
fun CustomRoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    backgroundColor: Color,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = Color.Gray) },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp), // Padding kiri kanan biar ga nempel pinggir
        shape = RoundedCornerShape(50), // Bikin rounded banget
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah saat aktif
            unfocusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah saat mati
        ),
        singleLine = true
    )
}

// User Data Class
data class UserData(
    val name: String,
    val isCurrentUser: Boolean = false,
    val avatarColor: Color? = null
)

// TODO: Tata cara penggunaan cukup cek preview langsung aja udah ada contoh

// Avatar List
@Composable
fun AvatarList(
    users: List<UserData> = emptyList(),
    avatarSize: Dp = 80.dp,
    showName: Boolean = true,
    showAddButton: Boolean = false,
    onAddClick: () -> Unit = {},
    onAvatarClick: (UserData) -> Unit = {}
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        // 1. Render List User
        items(users) { user ->
            AvatarItem(
                user = user,
                avatarSize = avatarSize,
                showName = showName,
                onClick = { onAvatarClick(user) }
            )
        }

        // 2. Render Tombol Add
        if (showAddButton) {
            item {
                AddAvatarButton(
                    avatarSize = avatarSize,
                    showName = showName,
                    onClick = onAddClick
                )
            }
        }
    }
}

// Avatar Item Helper Function
@Composable
private fun AvatarItem(
    user: UserData,
    avatarSize: Dp,
    showName: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(avatarSize)
            .clickable { onClick() }
    ) {
        // Lingkaran Avatar
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(user.avatarColor ?: getRandomAvatarColor(user.name)),
            contentAlignment = Alignment.Center
        ) {
            // Optional: Initials inside avatar
        }

        // Nama User
        if (showName) {
            Spacer(modifier = Modifier.height(8.dp))

            // PERBAIKAN DI SINI: Menggunakan androidx.compose.material3.Text secara eksplisit
            Text(
                text = if (user.isCurrentUser) "You" else user.name,
                color = Color.Black,
                fontSize = calculateFontSize(avatarSize),
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Add Avatar Button
@Composable
private fun AddAvatarButton(
    avatarSize: Dp,
    showName: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(avatarSize)
            .clickable { onClick() }
    ) {
        // Lingkaran Tombol Add
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add User",
                tint = Color.DarkGray,
                modifier = Modifier.size(avatarSize * 0.4f)
            )
        }

        // Spacer Text
        if (showName) {
            Spacer(modifier = Modifier.height(8.dp))

            // PERBAIKAN DI SINI: Menggunakan androidx.compose.material3.Text secara eksplisit
            Text(
                text = "",
                fontSize = calculateFontSize(avatarSize),
                maxLines = 1
            )
        }
    }
}

// More Helper Functions
private fun calculateFontSize(avatarSize: Dp): androidx.compose.ui.unit.TextUnit {
    return (avatarSize.value * 0.25).sp
}

// Bank Account Data Class
data class BankAccount(
    val bankName: String,
    val accountNumber: String,
    val bankColor: Color = Color(0xFF0066CC) // Default bank color
)

// TODO: Tata cara penggunaan cukup cek preview langsung aja udah ada contoh

// Contact Card Layout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCard(
    modifier: Modifier = Modifier,
    contactName: String,
    phoneNumber: String,
    avatarName: String = "avatar_1", // Avatar name dari database
    avatarColor: Color = Color(0xFF5FBDAC), // Default teal color jika avatar tidak ada
    events: List<String> = emptyList(),
    bankAccounts: List<BankAccount> = emptyList(),
    maxHeight: Dp? = null, // Ukuran maksimal tinggi card
    horizontalPadding: Dp = 16.dp, // Padding horizontal card
    verticalPadding: Dp = 16.dp, // Padding vertikal card
    innerPadding: Dp = 24.dp, // Padding dalam card
    avatarSize: Dp = 100.dp, // Ukuran avatar
    cornerRadius: Dp = 24.dp, // Radius sudut card
    onEditClicked: () -> Unit = {},
    onDeleteClicked: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (maxHeight != null) it.heightIn(max = maxHeight) else it }
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            // Header Section: Avatar + Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (Display actual avatar image or fallback to color)
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarName.isNotEmpty() && avatarName != "avatar_1") {
                        Image(
                            painter = painterResource(id = AvatarUtils.getAvatarResId(avatarName)),
                            contentDescription = "Contact Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contactName.take(1).uppercase(),
                                color = UIWhite,
                                fontSize = (avatarSize.value * 0.4).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action Buttons (Edit & Delete)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Edit Button
                    IconButton(
                        onClick = onEditClicked,
                        modifier = Modifier
                            .size(48.dp)
                            .background(UIAccentYellow, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Contact",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier
                            .size(48.dp)
                            .background(UIAccentYellow, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Contact",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Information Section
            Text(
                text = contactName,
                style = AppFont.Bold,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = phoneNumber,
                style = AppFont.Medium,
                fontSize = 18.sp,
                color = UIDarkGrey
            )

            // Events Section
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                val eventsText = formatEventsText(events)
                Text(
                    text = "Events: $eventsText",
                    fontSize = 16.sp,
                    style = AppFont.Medium,
                    color = Color.Black,
                    lineHeight = 24.sp
                )
            }

            // Bank Accounts Section
            if (bankAccounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                bankAccounts.forEach { bankAccount ->
                    Spacer(modifier = Modifier.height(12.dp))
                    BankAccountRow(bankAccount = bankAccount)
                }
            }
        }
    }
}

// Helper Function for Bank Account Row
@Composable
private fun BankAccountRow(bankAccount: BankAccount) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bank Logo
        val logoResId = when (bankAccount.bankName.lowercase().trim()) {
            "bca" -> R.drawable.bank_logo_bca
            "bri" -> R.drawable.bank_logo_bri
            "bni" -> R.drawable.bank_logo_bni
            "blu" -> R.drawable.bank_logo_blu
            "mandiri" -> R.drawable.bank_logo_mandiri
            else -> null
        }

        if (logoResId != null) {
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = "${bankAccount.bankName} Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            // Fallback jika logo tidak ditemukan
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bankAccount.bankColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bankAccount.bankName.take(3).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UIWhite
                )
            }
        }

        // Account Number
        Text(
            text = bankAccount.accountNumber,
            style = AppFont.Medium,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

// Helper Function for Events Text
/**
 * Formats the events list according to the requirements:
 * - Display max 3 events
 * - If more than 3, show first 3 and append ", X+ more"
 */
private fun formatEventsText(events: List<String>): String {
    return when {
        events.isEmpty() -> ""
        events.size <= 3 -> events.joinToString(", ")
        else -> {
            val displayedEvents = events.take(3).joinToString(", ")
            val remainingCount = events.size - 3
            "$displayedEvents, $remainingCount+ more"
        }
    }
}

/**
 * Data class to represent a receipt item
 * @param quantity The quantity of the item
 * @param itemName The name of the item
 * @param price The price of the item
 * @param members List of colors representing members (empty list means no members to display)
 */
data class ReceiptItem(
    val quantity: Int,
    val itemName: String,
    val price: Long,
    val members: List<Color> = emptyList()
)
// TODO: Tata cara penggunaan cukup cek preview langsung aja udah ada contoh

/**
 * Single receipt row composable
 * Displays quantity, item name, price, and members (if any)
 */
@Composable
fun ReceiptRow(
    item: ReceiptItem,
    avatarSize: Dp = 36.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        // Top Row: Quantity x Item Name | Price
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.quantity}x ${item.itemName}",
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatRupiah(item.price),
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        // Bottom Row: Member Avatars (only show if members list is not empty)
        if (item.members.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                item.members.forEach { memberColor ->
                    Surface(
                        modifier = Modifier
                            .size(avatarSize)
                            .padding(4.dp),
                        shape = CircleShape,
                        color = memberColor
                    ) {}
                }
            }
        }
    }
}

/**
 * Main receipt list composable
 * Displays a list of receipt items with horizontal dividers
 *
 * @param items List of receipt items to display
 * @param modifier Modifier for the LazyColumn
 * @param avatarSize Size of member avatar circles
 * @param horizontalPadding Horizontal padding for each item row
 * @param verticalPadding Vertical padding for each item row
 * @param maxHeight Optional max height for the list (scrollable if exceeded)
 * @param dividerPadding Horizontal padding for dividers
 * @param dividerThickness Thickness of divider lines
 * @param fontSize Font size for item name and price
 * @param spacerHeight Height of spacer between item name and avatars
 */
@Composable
fun ReceiptList(
    items: List<ReceiptItem>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 36.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp,
    maxHeight: Dp? = null,
    dividerPadding: Dp = 16.dp,
    dividerThickness: Dp = 1.dp,
    fontSize: Int = 16,
    spacerHeight: Dp = 8.dp
) {
    val listModifier = if (maxHeight != null) {
        modifier
            .fillMaxWidth()
            .background(Color.White)
            .height(maxHeight)
    } else {
        modifier
            .fillMaxWidth()
            .background(Color.White)
    }

    LazyColumn(modifier = listModifier) {
        items(items, key = { it.itemName }) { item ->
            ReceiptRow(
                item = item,
                avatarSize = avatarSize,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding
            )

            // Add divider after each item except the last
            if (items.indexOf(item) < items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dividerPadding),
                    color = UIGrey,
                    thickness = dividerThickness
                )
            }
        }
    }
}

/**
 * Format price to Indonesian Rupiah format
 */
fun formatRupiah(price: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    return formatter.format(price)
}

// Sidebar Layout
@Composable
fun SidebarContent(
    onCloseClick: () -> Unit = {}, // Placeholder untuk aksi tutup sidebar
    onDashboardClick: () -> Unit = {} // Callback untuk Dashboard
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .fillMaxHeight()
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 24.dp) // Padding keseluruhan
    ) {

        // --- HEADER (Logo, Nama App, Tombol Back) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp) // Jarak header ke menu item pertama
        ) {
            // Placeholder Logo (Lingkaran Kuning)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_luca_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nama App
            Text(
                text = "Luca",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f)) // Dorong panah ke kanan

            // Tombol Back (Panah Kiri)
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close Sidebar",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // --- MENU ITEMS ---
        // List menu
        SidebarMenuItem(icon = Icons.Outlined.Home, text = "Dashboard") { onDashboardClick() }
        SidebarMenuItem(icon = Icons.Outlined.Person, text = "Account") { /* Placeholder Click */ }
        SidebarMenuItem(icon = Icons.Outlined.Settings, text = "Settings") { /* Placeholder Click */ }
        SidebarMenuItem(icon = Icons.Outlined.Flag, text = "Report Bugs") { /* Placeholder Click */ }
        SidebarMenuItem(icon = Icons.Outlined.Info, text = "About Us") { /* Placeholder Click */ }

        // --- SPACER PENDORONG ---
        // Ini kuncinya: Spacer ini akan memakan semua ruang kosong yang tersisa
        Spacer(modifier = Modifier.weight(1f))

        // --- DIVIDER & FOOTER ---
        HorizontalDivider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Menu Footer (Help & Support)
        SidebarMenuItem(
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            text = "Help & Support"
        ) { /* Placeholder Click */ }
    }
}

// Komponen Helper untuk Item Menu supaya kodenya rapi
@Composable
fun SidebarMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp) // Jarak antar item
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Black, // Icon outline hitam sesuai gambar
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 18.sp,
            color = UIBlack, // Hitam agak soft dikit biar elegan
            style = AppFont.Medium
        )
    }
}

// Adaptive Search Bar
// TODO: Tata cara penggunaan cukup cek preview langsung aja udah ada contoh
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarModify(
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    initialQuery: String = "", // Ganti dari searchQuery
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    readOnly: Boolean = true,
    enabled: Boolean = true,
    databaseLabel: String? = null
) {
    // STATE INTERNAL - Otomatis handle input
    var internalSearchQuery by remember { mutableStateOf(initialQuery) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = UIWhite,
        shadowElevation = 2.dp,
        onClick = if (readOnly) { onSearchClick } else { {} }
    ) {
        // Database label disimpan internal saja, tidak ditampilkan di UI
        // Bisa digunakan untuk logging atau logic lainnya
        if (databaseLabel != null) {
            // TODO: nanti logic taro sini
            // Log atau logic internal bisa ditambahkan di sini
            // println("Searching in: $databaseLabel")
            @Suppress("UNUSED_EXPRESSION")
            databaseLabel // Placeholder to avoid empty body warning
        }

        if (readOnly) {
            // Mode Read-Only: Hanya tampilan, tidak bisa diisi
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = UIDarkGrey,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = placeholder,
                    style = AppFont.Regular,
                    fontSize = 16.sp,
                    color = UIDarkGrey
                )
            }
        } else {
            // Mode Editable: Bisa diisi text dengan STATE INTERNAL
            BasicTextField(
                value = internalSearchQuery,
                onValueChange = { newQuery ->
                    internalSearchQuery = newQuery
                    onSearchQueryChange(newQuery)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    // PENTING: Padding vertikal Container diatur disini
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                textStyle = AppFont.Regular.copy(
                    fontSize = 16.sp,
                    color = UIBlack // Pastikan warna text di set disini
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { innerTextField ->
                    // Kita harus menyusun ulang layoutnya secara manual (Row -> Icon -> Text)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. LEADING ICON
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = UIDarkGrey,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 2. PLACEHOLDER & TEXT FIELD
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Tampilkan Placeholder jika query kosong
                            if (internalSearchQuery.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = AppFont.Regular,
                                    fontSize = 16.sp,
                                    color = UIDarkGrey
                                )
                            }

                            // Ini adalah komponen input teks aslinya
                            // Karena BasicTextField tidak punya container,
                            // dia tidak akan memotong 'ekor' huruf
                            innerTextField()
                        }
                    }
                }
            )
//            TextField(
//                value = internalSearchQuery,
//                onValueChange = { newQuery ->
//                    internalSearchQuery = newQuery // Update state internal
//                    onSearchQueryChange(newQuery) // Kirim ke callback
//                },
//                modifier = Modifier.fillMaxWidth(),
//                placeholder = {
//                    Text(
//                        text = placeholder,
//                        style = AppFont.Regular,
//                        fontSize = 16.sp,
//                        color = UIDarkGrey
//                    )
//                },
//                leadingIcon = {
//                    Icon(
//                        imageVector = Icons.Default.Search,
//                        contentDescription = "Search",
//                        tint = UIDarkGrey,
//                        modifier = Modifier.size(20.dp)
//                    )
//                },
//                enabled = enabled,
//                singleLine = true,
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = Color.Transparent,
//                    unfocusedContainerColor = Color.Transparent,
//                    disabledContainerColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent,
//                    disabledIndicatorColor = Color.Transparent
//                ),
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//                textStyle = AppFont.Regular.copy(
//                    fontSize = 16.sp)
//            )
        }
    }
}

@Composable
fun BankAccountItem(
    bankName: String,
    accountNumber: String,
    bankLogoName: String,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val logoResId = remember(bankLogoName) {
        context.resources.getIdentifier(bankLogoName, "drawable", context.packageName)
    }
    val finalLogoId = if (logoResId != 0) logoResId else R.drawable.ic_launcher_foreground

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = UIGrey),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = finalLogoId),
                contentDescription = bankName,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = bankName, style = AppFont.SemiBold, fontSize = 14.sp, color = UIBlack)
                Text(text = accountNumber, style = AppFont.Medium, fontSize = 12.sp, color = UIDarkGrey)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "Delete", tint = UIDarkGrey, modifier = Modifier.size(16.dp))
            }
        }
    }
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

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
fun PreviewOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        UserProfileOverlay(
            onClose = { println("Overlay closed") },
            // PERBAIKAN DI SINI:
            // Lambda sekarang harus menerima 4 parameter: name, phone, banks, avatarName
            onAddContact = { name: String, phone: String, banks: List<BankAccountData>, avatarName: String ->
                println("Contact added: $name, $phone, $banks, $avatarName")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AvatarListPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        // Preview dengan androidx.compose.material3.Text juga
        Text("Preview: With Add Button")
        Spacer(modifier = Modifier.height(10.dp))

        // TODO:  Kalau mau pake copas saja dari sini
        AvatarList(
            users = listOf(
                UserData("You", true, Color(0xFFFF8C42)),
                UserData("Jeremy E"),
                UserData("Steven K")
            ),
            avatarSize = 60.dp,
            showName = true,
            showAddButton = true
        )
        // TODO: sampai sini teman teman
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0EDEA)
@Composable
fun ContactCardPreview() {
    LucaTheme {
        // TODO: Apabila ingin menggunakan cukup copy paste ini aja
        ContactCard(
            contactName = "Aldi Faustinus",
            phoneNumber = "+62 834 2464 3255",
            avatarColor = Color(0xFF5FBDAC),
            maxHeight = 600.dp,
            horizontalPadding = 5.dp,
            verticalPadding = 5.dp,
            events = listOf(
                "Bali with the boys",
                "Fancy dinner in PIK",
                "Roaming Jogja",
                "Beach Party",
                "Mountain Hiking"
            ),
            bankAccounts = listOf(
                BankAccount(
                    bankName = "BCA",
                    accountNumber = "5436774334",
                    bankColor = Color(0xFF0066CC)
                ),
                BankAccount(
                    bankName = "BRI",
                    accountNumber = "0023421568394593",
                    bankColor = Color(0xFF003D82)
                )
            ),
            onEditClicked = { /* Handle edit */ },
            onDeleteClicked = { /* Handle delete */ }
        )
        // TODO: sampai sini
    }
}

@Preview
@Composable
fun ReceiptListPreview() {
    LucaTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(UIWhite)
        ) {
            Column {

                Text(
                    text = "Default Receipt List",
                    modifier = Modifier.padding(16.dp),
                    style = AppFont.Bold
                )
                //TODO: ini kalau default copas aja teman teman dari sini
                ReceiptList(
                    items = listOf(
                        ReceiptItem(
                            quantity = 1,
                            itemName = "Gurame Bakar Kecap",
                            price = 120000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        ),
                        ReceiptItem(
                            quantity = 3,
                            itemName = "Nasi Putih",
                            price = 30000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        )
                    ),
                    modifier = Modifier.padding(vertical = 16.dp)

                    // TODO: sampai ini
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Example 2: Custom size with maxHeight
                Text(
                    text = "Custom Size with MaxHeight (200dp)",
                    modifier = Modifier.padding(16.dp),
                    style = AppFont.Bold
                )
                // TODO:"Ini kalau kalian mau menggunakan custom size receiptnya
                //TODO: copas dari sini
                ReceiptList(
                    items = listOf(
                        ReceiptItem(
                            quantity = 1,
                            itemName = "Gurame Bakar Kecap",
                            price = 120000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        ),
                        ReceiptItem(
                            quantity = 3,
                            itemName = "Nasi Putih",
                            price = 30000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        ),
                        ReceiptItem(
                            quantity = 2,
                            itemName = "Tumis Kangkung",
                            price = 50000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A)
                            )
                        ),
                        ReceiptItem(
                            quantity = 1,
                            itemName = "Chocolate Milkshake",
                            price = 27000,
                            members = listOf(Color(0xFF26A69A))
                        ),
                        ReceiptItem(
                            quantity = 2,
                            itemName = "Es Teh Manis",
                            price = 32000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFFFFA726)
                            )
                        )
                    ),

                    //costum size receiptnya
                    modifier = Modifier.padding(vertical = 60.dp, horizontal = 60.dp),
                    maxHeight = 200.dp,
                    avatarSize = 32.dp,
                    horizontalPadding = 5.dp,
                    verticalPadding = 5.dp
                )
                // TODO:sampai sini
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 800)
@Composable
fun PreviewSidebar() {
    SidebarContent()
}

@Preview
@Composable
fun ComponentsPreviewStv(){
    LucaTheme {
        Column(
            modifier = Modifier
                .background(UIWhite)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contoh 2: Mode Editable tanpa database
            Text("2. Editable Mode (Tanpa Database):", fontWeight = FontWeight.Bold)

            //TODO: COPAS AJA DARI SINI
            SearchBarModify(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp,vertical = 12.dp)
                    .height(50.dp),
                placeholder = "Search",
                onSearchQueryChange = { query ->
                    // Handle search query change
                    println("Search: $query")
                },
                readOnly = false
            )
            // TODO: SAMPAI SINI

            // Contoh 3: Mode Editable dengan database label
            Text("3. Editable Mode (Dengan Database):", fontWeight = FontWeight.Bold)
            //TODO: Kalau pake database copas dari sini
            SearchBarModify(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp,vertical = 12.dp)
                    .height(50.dp),
                placeholder = "Search products...",
                onSearchQueryChange = { query ->
                    println("Searching in database: $query")
                },
                readOnly = false,
                databaseLabel = "Database: Products"
            )
            //TODO: sampai sini
        }
    }
}














