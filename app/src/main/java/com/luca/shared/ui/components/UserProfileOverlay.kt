package com.luca.shared.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.luca.shared.R
import com.luca.shared.model.BankAccountData
import com.luca.shared.model.Contact
import com.luca.shared.ui.theme.*
import com.luca.shared.util.AvatarUtils
import com.luca.shared.util.BankUtils

@Composable
fun UserProfileOverlay(
    onClose: () -> Unit,
    onAddContact: (String, String, List<BankAccountData>, String) -> Unit,
    editContact: Contact? = null,
    onUpdateContact: (String, String, String, String, List<BankAccountData>, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    var username by remember { mutableStateOf(editContact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(editContact?.phoneNumber ?: "") }

    // Bank accounts management
    var bankAccounts by remember { mutableStateOf<List<BankAccountData>>(editContact?.bankAccounts ?: emptyList()) }
    var showBankDialog by remember { mutableStateOf(false) }

    // Avatar management - TAP TO SHUFFLE
    var selectedAvatarSeed by remember { mutableStateOf(editContact?.avatarName ?: "") }
    var tapCount by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    // Handle avatar tap untuk shuffle
    val handleAvatarTap: () -> Unit = {
        val baseName = username.ifEmpty { "User" }
        tapCount++
        selectedAvatarSeed = "$baseName$tapCount"
    }

    val handleAddOrUpdate: () -> Unit = {
        if (username.isNotEmpty()) {
            val finalSeed = if (selectedAvatarSeed.isNotEmpty()) selectedAvatarSeed else username

            if (editContact != null) {
                onUpdateContact(
                    editContact.id,
                    username,
                    phoneNumber,
                    editContact.description ?: "",
                    bankAccounts,
                    finalSeed
                )
            } else {
                onAddContact(
                    username,
                    phoneNumber,
                    bankAccounts,
                    finalSeed
                )
            }
        } else {
            Toast.makeText(context, "Username wajib diisi!", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close Button
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Close",
                    tint = UIBlack,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (editContact != null) "Edit Contact" else "Add New Contact",
                style = AppFont.SemiBold,
                fontSize = 20.sp,
                color = UIBlack
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== AVATAR - TAP TO SHUFFLE =====
            val displaySeed = if (selectedAvatarSeed.isNotEmpty()) selectedAvatarSeed else (username.ifEmpty { "User" })
            val avatarUrl = remember(displaySeed) { AvatarUtils.getDiceBearUrl(displaySeed) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(UIGrey)
                    .clickable { handleAvatarTap() }
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Avatar - Tap to shuffle",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),

                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = UIBlack,
                                strokeWidth = 2.dp
                            )
                        }
                    },

                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getRandomAvatarColor(username.ifEmpty { "User" })),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = username.take(1).uppercase().ifEmpty { "U" },
                                style = AppFont.SemiBold,
                                color = UIWhite,
                                fontSize = 40.sp
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap avatar to shuffle",
                style = AppFont.Medium,
                fontSize = 12.sp,
                color = UIAccentYellow
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Username Input
            ProfileInputForm(
                text = username,
                onValueChange = { username = it },
                placeholder = "Username",
                iconRes = R.drawable.ic_user_form,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Input - TANPA ICON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(color = UIGrey, shape = RoundedCornerShape(23.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (phoneNumber.isEmpty()) {
                    Text(
                        text = "Phone Number",
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        color = UIBlack.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    textStyle = TextStyle(
                        fontFamily = AppFont.Medium.fontFamily,
                        fontSize = 14.sp,
                        color = UIBlack
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== BANK ACCOUNTS SECTION =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bank Accounts",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                IconButton(
                    onClick = { showBankDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Bank",
                        tint = UIAccentYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Display existing bank accounts
            if (bankAccounts.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bankAccounts.forEach { bankAccount ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = UIGrey),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Bank logo
                                val logoResId = remember(bankAccount.bankName) {
                                    when (bankAccount.bankName.lowercase()) {
                                        "bca" -> R.drawable.bank_logo_bca
                                        "bri" -> R.drawable.bank_logo_bri
                                        "bni" -> R.drawable.bank_logo_bni
                                        "mandiri" -> R.drawable.bank_logo_mandiri
                                        "blu" -> R.drawable.bank_logo_blu
                                        else -> R.drawable.bank_logo_bca // default
                                    }
                                }

                                Image(
                                    painter = painterResource(id = logoResId),
                                    contentDescription = bankAccount.bankName,
                                    modifier = Modifier.size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bankAccount.bankName,
                                        style = AppFont.SemiBold,
                                        fontSize = 14.sp,
                                        color = UIBlack
                                    )
                                    Text(
                                        text = bankAccount.accountNumber,
                                        style = AppFont.Regular,
                                        fontSize = 12.sp,
                                        color = UIDarkGrey
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        bankAccounts = bankAccounts.filter { it != bankAccount }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = androidx.compose.ui.graphics.Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = handleAddOrUpdate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                )
            ) {
                Text(
                    text = if (editContact != null) "Update Contact" else "Add Contact",
                    style = AppFont.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Bank Account Dialog
    if (showBankDialog) {
        AddBankAccountDialog(
            onDismiss = { showBankDialog = false },
            onAddBank = { bankName, accountNumber ->
                val newBank = BankAccountData(
                    bankName = bankName,
                    accountNumber = accountNumber,
                    bankLogo = BankUtils.generateLogoFileName(bankName)
                )
                bankAccounts = bankAccounts + newBank
                showBankDialog = false
            }
        )
    }
}

@Composable
fun ProfileInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(color = UIGrey, shape = RoundedCornerShape(23.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = UIBlack,
                modifier = Modifier.size(width = 16.dp, height = 16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        color = UIBlack.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = AppFont.Medium.fontFamily,
                        fontSize = 14.sp,
                        color = UIBlack
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun getRandomAvatarColor(name: String): androidx.compose.ui.graphics.Color {
    val colors = listOf(
        androidx.compose.ui.graphics.Color(0xFFEF5350),
        androidx.compose.ui.graphics.Color(0xFF42A5F5),
        androidx.compose.ui.graphics.Color(0xFF66BB6A),
        androidx.compose.ui.graphics.Color(0xFFFFA726),
        androidx.compose.ui.graphics.Color(0xFFAB47BC),
        androidx.compose.ui.graphics.Color(0xFF5FBDAC)
    )
    return colors[kotlin.math.abs(name.hashCode()) % colors.size]
}

@Composable
fun AddBankAccountDialog(
    onDismiss: () -> Unit,
    onAddBank: (String, String) -> Unit
) {
    var selectedBank by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val availableBanks = BankUtils.availableBanks

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UIWhite,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Add Bank Account",
                style = AppFont.SemiBold,
                fontSize = 18.sp,
                color = UIBlack
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Bank Selection with logo preview
                Text(
                    text = "Select Bank",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = UIBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableBanks.forEach { bankName ->
                        val isSelected = selectedBank == bankName
                        val logoResId = remember(bankName) {
                            when (bankName.lowercase()) {
                                "bca" -> R.drawable.bank_logo_bca
                                "bri" -> R.drawable.bank_logo_bri
                                "bni" -> R.drawable.bank_logo_bni
                                "mandiri" -> R.drawable.bank_logo_mandiri
                                "blu" -> R.drawable.bank_logo_blu
                                else -> R.drawable.bank_logo_bca
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) UIAccentYellow.copy(alpha = 0.2f) else UIGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBank = bankName
                                    errorMessage = ""
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = logoResId),
                                    contentDescription = bankName,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = bankName,
                                    style = AppFont.Medium,
                                    fontSize = 14.sp,
                                    color = UIBlack
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account Number Input
                Text(
                    text = "Account Number",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = UIBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(color = UIGrey, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (accountNumber.isEmpty()) {
                        Text(
                            text = "Enter account number",
                            style = AppFont.Medium,
                            fontSize = 14.sp,
                            color = UIBlack.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    BasicTextField(
                        value = accountNumber,
                        onValueChange = {
                            accountNumber = it
                            errorMessage = ""
                        },
                        textStyle = TextStyle(
                            fontFamily = AppFont.Medium.fontFamily,
                            fontSize = 14.sp,
                            color = UIBlack
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = androidx.compose.ui.graphics.Color(0xFFE53935),
                        fontSize = 12.sp,
                        style = AppFont.Regular
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        selectedBank.isEmpty() -> {
                            errorMessage = "Please select a bank"
                        }
                        accountNumber.isEmpty() -> {
                            errorMessage = "Please enter account number"
                        }
                        else -> {
                            onAddBank(selectedBank, accountNumber)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add", color = UIBlack, style = AppFont.SemiBold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = UIBlack, style = AppFont.Medium)
            }
        }
    )
}

