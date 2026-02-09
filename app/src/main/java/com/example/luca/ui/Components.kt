package com.example.luca.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.luca.R
import com.example.luca.model.BankAccountData
import com.example.luca.model.Contact
import com.example.luca.model.Event
import com.example.luca.ui.theme.*
import com.example.luca.util.AvatarUtils
import com.example.luca.util.BankUtils
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

// --- DATA CLASSES (DEFINISI TUNGGAL) ---

data class UserData(
    val name: String,
    val isCurrentUser: Boolean = false,
    val avatarColor: Color? = null
)

data class BankAccount(
    val bankName: String,
    val accountNumber: String,
    val bankColor: Color = Color(0xFF0066CC)
)

data class ReceiptItem(
    val quantity: Int,
    val itemName: String,
    val price: Long,
    val members: List<Color> = emptyList(),
    val memberNames: List<String> = emptyList()
)

enum class HeaderState(
    val title: String,
    val showLeftIconAsBack: Boolean,
    val showRightLogo: Boolean
) {
    HOME("Luca", false, true),
    NEW_EVENT("New Event", true, false),
    EDIT_EVENT("Editing Event", true, false),
    NEW_ACTIVITY("New Activity", true, false),
    DETAILS("Activity Details", true, false),
    EDIT_ACTIVITY("Edit Activity", true, false),
    ACCOUNT_SETTINGS("Account Settings", true, false),
    SUMMARY("Summary", true, false)
}

// --- HELPER FUNCTIONS (PRIVATE) ---

private fun calculateFontSizeHelper(avatarSize: Dp): TextUnit {
    return (avatarSize.value * 0.25).sp
}

private fun generateRandomColorHelper(name: String): Color {
    val colors = listOf(
        Color(0xFFFF8C42), Color(0xFF5FBDAC), Color(0xFF6B4B9E),
        Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
        Color(0xFFFDD835), Color(0xFFFB8C00), Color(0xFF6D4C41),
        Color(0xFF757575), Color(0xFF546E7A)
    )
    val index = kotlin.math.abs(name.hashCode()) % colors.size
    return colors[index]
}

// --- CORE COMPONENTS ---

@Composable
fun HeaderSection(
    currentState: HeaderState = HeaderState.HOME,
    onLeftIconClick: () -> Unit = {},
    onTitleDebugClick: () -> Unit = {}
) {
    Surface(
        color = UIWhite,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(60.dp)
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
                .background(UIWhite),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // KIRI: Icon (Hamburger / Back)
            Box(
                modifier = Modifier.fillMaxHeight().width(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(
                    targetState = currentState.showLeftIconAsBack,
                    label = "LeftIconAnim"
                ) { isBack ->
                    IconButton(onClick = onLeftIconClick) {
                        Icon(
                            painter = painterResource(if (isBack) R.drawable.ic_arrow_back else R.drawable.ic_hamburger_sidebar),
                            contentDescription = "Nav",
                            tint = UIBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // TENGAH: Title
            AnimatedContent(
                targetState = currentState.title,
                label = "TitleAnim"
            ) { titleText ->
                Text(
                    text = titleText,
                    color = UIBlack,
                    style = AppFont.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTitleDebugClick() }
                )
            }

            // KANAN: Logo (Jika ada)
            Box(
                modifier = Modifier.fillMaxHeight().width(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                this@Row.AnimatedVisibility(
                    visible = currentState.showRightLogo,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    // Logo ukurannya dinormalkan menjadi 30.dp
                    Image(
                        painter = painterResource(id = R.drawable.ic_luca_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingNavbar(
    selectedIndex: Int = 1,
    onItemSelected: (Int) -> Unit,
    onContactsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
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
            .offset(y = (-23).dp)
            .width(225.dp)
            .height(75.dp),
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFFFFC107),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .size(59.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(30.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(30.dp))
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIconButton(
                    iconRes = R.drawable.ic_scan_button,
                    desc = "Scan",
                    isSelected = selectedIndex == 0,
                    onClick = { onItemSelected(0) }
                )

                val centerIconRes = if (selectedIndex == 1) R.drawable.ic_plus_button else R.drawable.ic_home_button

                NavIconButton(
                    iconRes = centerIconRes,
                    desc = "Home/Plus",
                    isSelected = selectedIndex == 1,
                    onClick = {
                        if (selectedIndex == 1) onAddClick() else { onItemSelected(1); onHomeClick() }
                    }
                )

                NavIconButton(
                    iconRes = R.drawable.ic_contacts_button,
                    desc = "Contacts",
                    isSelected = selectedIndex == 2,
                    onClick = { onItemSelected(2); onContactsClick() }
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
            tint = UIBlack
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
            placeholder = { Text(text = placeholder, color = UIDarkGrey) },
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
                .size(60.dp)
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .width(220.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = UIAccentYellow,
            disabledContainerColor = UIAccentYellow.copy(alpha = 0.5f)
        ),
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
    itemSize: Dp = 40.dp
) {
    val showOverflow = avatars.size > maxVisible
    val displayCount = if (showOverflow) maxVisible - 1 else avatars.size
    val remaining = avatars.size - displayCount

    Row(
        horizontalArrangement = Arrangement.spacedBy((spacing).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Render Avatar yang terlihat
        for (i in 0 until displayCount) {
            // ZIndex agar avatar kiri menumpuk di atas avatar kanan (opsional, sesuaikan selera)
            // Kode B tidak pake zIndex manual, tapi urutan draw compose defaultnya kiri di bawah kanan.
            // Kalau mau kiri diatas kanan, pake zIndex menurun.
            Box(modifier = Modifier.zIndex((displayCount - i).toFloat())) {
                AvatarItem(
                    avatarName = avatars[i],
                    size = itemSize
                )
            }
        }

        // 2. Render Lingkaran Sisa (+N) jika ada
        if (showOverflow && remaining > 0) {
            Box(
                modifier = Modifier
                    .size(itemSize)
                    .clip(CircleShape)
                    .border(2.dp, UIWhite, CircleShape)
                    .background(UIBlack), // Sesuaikan warna background sisa (+N)
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remaining",
                    color = UIWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AvatarItem(
    avatarName: String,
    size: Dp = 40.dp
) {
    val resId = remember(avatarName) {
        AvatarUtils.getAvatarResId(avatarName)
    }

    Image(
        painter = painterResource(id = resId),
        contentDescription = "Avatar",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, UIWhite, CircleShape) // Border putih agar keliatan pisahnya pas ditumpuk
            .background(Color.LightGray) // Fallback background
    )
}

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
        colors = CardDefaults.cardColors(containerColor = UIWhite)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(all = 15.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(15.dp))
                            .background(UIGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(event.imageUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Event Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = null,
                            error = painterResource(id = R.drawable.ic_launcher_foreground)
                        )

                        Box(modifier = Modifier.padding(10.dp)) {
                            StackedAvatarRow(
                                itemSize = 36.dp,
                                avatars = event.participantAvatars
                            )
                        }
                    }
                }

                Text(
                    text = event.title,
                    color = UIBlack,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    style = AppFont.SemiBold,
                    fontSize = 20.sp,
                    maxLines = 1
                )

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

@Suppress("unused")
@SuppressLint("LocalContextResourcesRead")
@Composable
fun getResourceId(name: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(
        "avatar_$name",
        "drawable",
        context.packageName
    )
}

@Composable
fun UserProfileOverlay(
    onClose: () -> Unit,
    onAddContact: (String, String, List<BankAccountData>, String) -> Unit,
    editContact: Contact? = null,
    onUpdateContact: ((String, String, String, String, List<BankAccountData>, String) -> Unit)? = null
) {
    var name by remember { mutableStateOf(editContact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(editContact?.phoneNumber ?: "") }
    var description by remember { mutableStateOf(editContact?.description ?: "") }
    var bankAccounts by remember { mutableStateOf<List<BankAccountData>>(editContact?.bankAccounts ?: emptyList()) }
    var showBankDialog by remember { mutableStateOf(false) }
    var bankFullError by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var selectedAvatarName by remember { mutableStateOf(editContact?.avatarName ?: "") }
    var showNameError by remember { mutableStateOf(false) }

    LaunchedEffect(bankFullError) {
        if (bankFullError) {
            delay(3000)
            bankFullError = false
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp).wrapContentHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(32.dp), tint = UIBlack)
                }

                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).clickable { showAvatarDialog = true }.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedAvatarName.isNotEmpty()) {
                        Image(
                            painter = painterResource(id = AvatarUtils.getAvatarResId(selectedAvatarName)),
                            contentDescription = "Selected Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(UIGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, "Select Avatar", tint = UIDarkGrey, modifier = Modifier.size(40.dp))
                        }
                    }
                }

                IconButton(
                    onClick = {
                        if (name.isBlank()) {
                            showNameError = true
                        } else {
                            if (editContact != null && onUpdateContact != null) {
                                onUpdateContact(editContact.id, name, phoneNumber, description, bankAccounts, selectedAvatarName)
                            } else {
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

            CustomRoundedTextField(value = name, onValueChange = { name = it; showNameError = false }, placeholder = "Name", backgroundColor = UIGrey)

            if (showNameError) {
                Text(
                    text = "Nama contact harus diisi",
                    color = Color.Red,
                    fontSize = 12.sp,
                    style = AppFont.Regular,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            CustomRoundedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, placeholder = "Phone Number (Optional)", backgroundColor = UIGrey)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Bank Accounts", fontSize = 18.sp, style = AppFont.Bold, color = UIBlack)
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(50.dp).clip(CircleShape)
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

            if (bankAccounts.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    bankAccounts.forEach { account ->
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

            if (bankFullError) {
                Text("Bank account is full", color = Color.Red, fontSize = 12.sp, style = AppFont.Regular, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 8.dp))
            }
        }
    }

    if (showBankDialog) {
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
    selectedBank: String,
    onBankSelected: (String) -> Unit,
    accountNumber: String,
    onAccountNumberChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    bankAccountCount: Int = 0,
    bankFullError: Boolean = false
) {
    val bankList = BankUtils.availableBanks
    val isAtMaxCapacity = bankAccountCount >= 3

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UIWhite),
            modifier = Modifier.fillMaxWidth(0.85f).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add Bank Account", fontSize = 18.sp, color = UIBlack, modifier = Modifier.padding(bottom = 16.dp))

                if (bankFullError) {
                    Text("Bank account is full", color = Color.Red, fontSize = 12.sp, style = AppFont.Regular, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                }

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

@Suppress("unused")
@Composable
fun BankButton(
    bankName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) UIAccentYellow else UIGrey),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(4.dp).height(40.dp)
    ) {
        Text(text = bankName, color = if (isSelected) Color.Black else UIDarkGrey, style = AppFont.Medium, fontSize = 12.sp)
    }
}

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        singleLine = true
    )
}

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
        items(users) { user ->
            AvatarListItem(user, avatarSize, showName) { onAvatarClick(user) }
        }
        if (showAddButton) {
            item {
                AddAvatarButton(avatarSize, showName, onAddClick)
            }
        }
    }
}

@Composable
private fun AvatarListItem(
    user: UserData,
    avatarSize: Dp,
    showName: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(avatarSize).clickable { onClick() }) {
        Box(modifier = Modifier.size(avatarSize).clip(CircleShape).background(user.avatarColor ?: generateRandomColorHelper(user.name)), contentAlignment = Alignment.Center) {}
        if (showName) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = if (user.isCurrentUser) "You" else user.name, color = Color.Black, fontSize = calculateFontSizeHelper(avatarSize), fontWeight = FontWeight.Normal, fontFamily = FontFamily.Default, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AddAvatarButton(
    avatarSize: Dp,
    showName: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(avatarSize).clickable { onClick() }) {
        Box(modifier = Modifier.size(avatarSize).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add User", tint = Color.DarkGray, modifier = Modifier.size(avatarSize * 0.4f))
        }
        if (showName) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "", fontSize = calculateFontSizeHelper(avatarSize), maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCard(
    modifier: Modifier = Modifier,
    contactName: String,
    phoneNumber: String,
    avatarName: String = "avatar_1",
    avatarColor: Color = Color(0xFF5FBDAC),
    events: List<String> = emptyList(),
    bankAccounts: List<BankAccount> = emptyList(),
    maxHeight: Dp? = null,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    innerPadding: Dp = 24.dp,
    avatarSize: Dp = 100.dp,
    cornerRadius: Dp = 24.dp,
    onEditClicked: () -> Unit = {},
    onDeleteClicked: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth().let { if (maxHeight != null) it.heightIn(max = maxHeight) else it }.padding(horizontal = horizontalPadding, vertical = verticalPadding),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(innerPadding)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(avatarSize).clip(CircleShape), contentAlignment = Alignment.Center) {
                    if (avatarName.isNotEmpty() && avatarName != "avatar_1") {
                        Image(painter = painterResource(id = AvatarUtils.getAvatarResId(avatarName)), contentDescription = "Contact Avatar", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(avatarColor), contentAlignment = Alignment.Center) {
                            Text(text = contactName.take(1).uppercase(), color = UIWhite, fontSize = (avatarSize.value * 0.4).sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    IconButton(onClick = onEditClicked, modifier = Modifier.size(48.dp).background(UIAccentYellow, CircleShape)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Contact", tint = Color.Black, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onDeleteClicked, modifier = Modifier.size(48.dp).background(UIAccentYellow, CircleShape)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Contact", tint = Color.Black, modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = contactName, style = AppFont.Bold, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = phoneNumber, style = AppFont.Medium, fontSize = 18.sp, color = UIDarkGrey)
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                val eventsText = formatEventsText(events)
                Text(text = "Events: $eventsText", fontSize = 16.sp, style = AppFont.Medium, color = Color.Black, lineHeight = 24.sp)
            }
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

@Composable
private fun BankAccountRow(bankAccount: BankAccount) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val logoResId = when (bankAccount.bankName.lowercase().trim()) { "bca" -> R.drawable.bank_logo_bca; "bri" -> R.drawable.bank_logo_bri; "bni" -> R.drawable.bank_logo_bni; "blu" -> R.drawable.bank_logo_blu; "mandiri" -> R.drawable.bank_logo_mandiri; else -> null }
        if (logoResId != null) {
            Image(painter = painterResource(id = logoResId), contentDescription = "${bankAccount.bankName} Logo", modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)))
        } else {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(bankAccount.bankColor), contentAlignment = Alignment.Center) {
                Text(text = bankAccount.bankName.take(3).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UIWhite)
            }
        }
        Text(text = bankAccount.accountNumber, style = AppFont.Medium, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

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

@Composable
fun ReceiptRow(
    item: ReceiptItem,
    avatarSize: Dp = 36.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = verticalPadding)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${item.quantity}x ${item.itemName}", style = AppFont.Bold, fontSize = 16.sp, color = Color.Black, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = formatRupiah(item.price), style = AppFont.Bold, fontSize = 16.sp, color = Color.Black)
        }
        if (item.members.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                item.members.forEach { memberColor ->
                    Surface(modifier = Modifier.size(avatarSize).padding(4.dp), shape = CircleShape, color = memberColor) {}
                }
            }
        }
    }
}

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
    val listModifier = if (maxHeight != null) modifier.fillMaxWidth().background(Color.White).height(maxHeight) else modifier.fillMaxWidth().background(Color.White)
    LazyColumn(modifier = listModifier) {
        items(items, key = { it.itemName }) { item ->
            ReceiptRow(item = item, avatarSize = avatarSize, horizontalPadding = horizontalPadding, verticalPadding = verticalPadding)
            if (items.indexOf(item) < items.size - 1) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = dividerPadding), color = UIGrey, thickness = dividerThickness)
            }
        }
    }
}

fun formatRupiah(price: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    return formatter.format(price)
}

// Sidebar Layout
@Composable
fun SidebarContent(
    onCloseClick: () -> Unit = {},
    onDashboardClick: () -> Unit = {},
    onAccountSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onReportBugClick: () -> Unit = {},
    onAboutUsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var expandAccountMenu by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = UIWhite,
            shape = RoundedCornerShape(24.dp),
            icon = { Box(modifier = Modifier.size(72.dp).background(UIAccentYellow.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Logout", tint = UIAccentYellow, modifier = Modifier.size(32.dp)) } },
            title = { Text(text = "Keluar Akun?", style = AppFont.Bold, fontSize = 20.sp, color = UIBlack, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Kamu harus login ulang untuk mengakses data Luca.", color = UIDarkGrey, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { showLogoutDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = UIGrey, contentColor = UIBlack), shape = RoundedCornerShape(50.dp), elevation = ButtonDefaults.buttonElevation(0.dp), modifier = Modifier.height(48.dp).weight(1f)) { Text(text = "Batal", style = AppFont.SemiBold, fontSize = 14.sp) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { showLogoutDialog = false; onLogoutClick() }, colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow, contentColor = UIBlack), shape = RoundedCornerShape(50), elevation = ButtonDefaults.buttonElevation(0.dp), modifier = Modifier.height(48.dp).weight(1f)) { Text(text = "Ya", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(horizontal = 14.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)) {
            Box(modifier = Modifier.size(30.dp).clip(CircleShape)) { Image(painter = painterResource(R.drawable.ic_luca_logo), contentDescription = "Logo", modifier = Modifier.fillMaxSize()) }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Luca", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCloseClick) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Sidebar", tint = Color.Black, modifier = Modifier.size(28.dp)) }
        }
        SidebarMenuItem(icon = Icons.Outlined.Home, text = "Dashboard") { onDashboardClick() }

        // Account Menu with Dropdown
        SidebarMenuItemExpandable(
            icon = Icons.Outlined.Person,
            text = "Account",
            isExpanded = expandAccountMenu,
            onClick = { expandAccountMenu = !expandAccountMenu }
        )
        if (expandAccountMenu) {
            SidebarSubMenuItem(text = "Account Settings") {
                expandAccountMenu = false
                onAccountSettingsClick()
            }
            SidebarSubMenuItem(text = "Logout") {
                expandAccountMenu = false
                showLogoutDialog = true
            }
        }

        SidebarMenuItem(icon = Icons.Outlined.Settings, text = "Settings") { onSettingsClick() }
        SidebarMenuItem(icon = Icons.Outlined.Flag, text = "Report Bugs") { onReportBugClick() }
        SidebarMenuItem(icon = Icons.Outlined.Info, text = "About Us") { onAboutUsClick() }
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(bottom = 24.dp))
        SidebarMenuItem(icon = Icons.AutoMirrored.Outlined.HelpOutline, text = "Help & Support") { onHelpSupportClick() }
    }
}

@Composable
fun SidebarMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable(onClick = onClick)) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.Black, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 18.sp, color = UIBlack, style = AppFont.Medium)
    }
}

@Composable
fun SidebarMenuItemExpandable(
    icon: ImageVector,
    text: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.Black, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 18.sp, color = UIBlack, style = AppFont.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = "Expand",
            tint = UIDarkGrey,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SidebarSubMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Spacer(modifier = Modifier.width(42.dp))
        Text(text = text, fontSize = 16.sp, color = UIBlack, style = AppFont.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarModify(
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    initialQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    readOnly: Boolean = true,
    enabled: Boolean = true,
    databaseLabel: String? = null
) {
    var internalSearchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }

    // Update internal state when initialQuery changes
    LaunchedEffect(initialQuery) {
        internalSearchQuery = initialQuery
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = UIWhite,
        shadowElevation = 2.dp,
        onClick = if (readOnly) { onSearchClick } else { {} }
    ) {
        if (readOnly) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
            BasicTextField(
                value = internalSearchQuery,
                onValueChange = { newQuery ->
                    internalSearchQuery = newQuery
                    onSearchQueryChange(newQuery)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                textStyle = AppFont.Regular.copy(fontSize = 16.sp, color = UIBlack),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                enabled = enabled,
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = UIDarkGrey,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (internalSearchQuery.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = AppFont.Regular,
                                    fontSize = 16.sp,
                                    color = UIDarkGrey
                                )
                            }
                            innerTextField()
                        }
                        // Show clear button when there's text
                        if (internalSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    internalSearchQuery = ""
                                    onSearchQueryChange("")
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = UIDarkGrey,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Reusable component to display "Not Found" message when search or filter returns empty results
 *
 * @param searchQuery The current search query (optional)
 * @param emptyStateMessage Message to show when no search is active
 * @param notFoundMessage Custom message when search returns no results
 * @param showIcon Whether to show a search icon
 */
@Composable
fun NotFoundMessage(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    emptyStateMessage: String = "No items found",
    notFoundMessage: String? = null,
    showIcon: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = if (searchQuery.isEmpty()) Icons.Default.Info else Icons.Default.Search,
                    contentDescription = null,
                    tint = UIDarkGrey.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = if (searchQuery.isEmpty()) {
                    emptyStateMessage
                } else {
                    notFoundMessage ?: "No results found for \"$searchQuery\""
                },
                textAlign = TextAlign.Center,
                style = AppFont.Regular,
                color = UIDarkGrey,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
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
    val logoResId = remember(bankLogoName) { context.resources.getIdentifier(bankLogoName, "drawable", context.packageName) }
    val finalLogoId = if (logoResId != 0) logoResId else R.drawable.ic_launcher_foreground
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = UIGrey), modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = finalLogoId), contentDescription = bankName, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(4.dp), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(text = bankName, style = AppFont.SemiBold, fontSize = 14.sp, color = UIBlack); Text(text = accountNumber, style = AppFont.Medium, fontSize = 12.sp, color = UIDarkGrey) }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, "Delete", tint = UIDarkGrey, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
fun AvatarSelectionOverlay(
    currentSelection: String,
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().background(UIWhite, shape = RoundedCornerShape(16.dp)).padding(20.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Select an Avatar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UIBlack)
                Spacer(modifier = Modifier.height(20.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(300.dp)) {
                    items(AvatarUtils.avatars) { (name, resId) ->
                        val isSelected = name == currentSelection
                        Box(modifier = Modifier.aspectRatio(1f).clip(CircleShape).border(width = if (isSelected) 3.dp else 0.dp, color = if (isSelected) UIAccentYellow else Color.Transparent, shape = CircleShape).clickable { onAvatarSelected(name); onDismiss() }) {
                            Image(painter = painterResource(id = resId), contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactSelectionOverlay(
    availableContacts: List<Contact>,
    selectedContacts: List<Contact>,
    onDismiss: () -> Unit,
    onConfirm: (List<Contact>) -> Unit,
    onAddNewContact: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedContacts) }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = UIWhite), modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 600.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Select Participants", style = AppFont.Bold, fontSize = 18.sp, color = UIBlack)
            Spacer(modifier = Modifier.height(16.dp))
            if (availableContacts.isEmpty()) { Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No contacts available", color = UIDarkGrey) } } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(availableContacts) { contact ->
                        val isSelected = currentSelection.any { it.name == contact.name }
                        ContactSelectionItem(contact = contact, isSelected = isSelected, onClick = { if (isSelected) currentSelection = currentSelection.filter { it.name != contact.name } else currentSelection = currentSelection + contact })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onAddNewContact, colors = ButtonDefaults.buttonColors(containerColor = UIGrey), modifier = Modifier.weight(1f).padding(end = 8.dp)) { Text("New Contact", color = UIBlack) }
                Button(onClick = { onConfirm(currentSelection) }, colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow), modifier = Modifier.weight(1f).padding(start = 8.dp)) { Text("Done", color = UIBlack) }
            }
        }
    }
}

@Composable
fun ContactSelectionItem(
    contact: Contact,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.background(if (isSelected) UIAccentYellow.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(12.dp)).border(width = if (isSelected) 2.dp else 0.dp, color = if (isSelected) UIAccentYellow else Color.Transparent, shape = RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = AvatarUtils.getAvatarResId(contact.avatarName)), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = contact.name, style = AppFont.Medium, fontSize = 14.sp, color = UIBlack, modifier = Modifier.weight(1f))
        if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = UIAccentYellow, modifier = Modifier.size(24.dp)) else Icon(Icons.Default.AddCircleOutline, contentDescription = "Select", tint = UIDarkGrey, modifier = Modifier.size(24.dp))
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
fun SidebarPreview() {
    LucaTheme {
        SidebarContent()
    }
}

//@Preview
//@Composable
//fun ComponentsPreview() {
//    LucaTheme {
//        Column(modifier = Modifier.background(UIWhite).padding(16.dp)) { PrimaryButton(text = "Test Button", onClick = {}) }
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFF888888)
//@Composable
//fun PreviewOverlay() {
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { UserProfileOverlay(onClose = { println("Overlay closed") }, onAddContact = { name: String, phone: String, banks: List<BankAccountData>, avatarName: String -> println("Contact added: $name, $phone, $banks, $avatarName") }) }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun AvatarListPreview() {
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Preview: With Add Button")
//        Spacer(modifier = Modifier.height(10.dp))
//        AvatarList(users = listOf(UserData("You", true, Color(0xFFFF8C42)), UserData("Jeremy E"), UserData("Steven K")), avatarSize = 60.dp, showName = true, showAddButton = true)
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFF0EDEA)
//@Composable
//fun ContactCardPreview() {
//    LucaTheme {
//        ContactCard(contactName = "Aldi Faustinus", phoneNumber = "+62 834 2464 3255", avatarColor = Color(0xFF5FBDAC), maxHeight = 600.dp, horizontalPadding = 5.dp, verticalPadding = 5.dp, events = listOf("Bali", "Dinner"), bankAccounts = listOf(BankAccount("BCA", "5436774334", Color(0xFF0066CC)), BankAccount("BRI", "0023421568394593", Color(0xFF003D82))))
//    }
//}