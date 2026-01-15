package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIWhite

// Data class for Bank Account
data class BankAccount(
    val bankName: String,
    val accountNumber: String,
    val bankColor: Color = Color(0xFF0066CC) // Default bank color
)

// TODO: Tata cara penggunaan cukup cek preview langsung aja udah ada contoh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactCard(
    modifier: Modifier = Modifier,
    contactName: String,
    phoneNumber: String,
    avatarColor: Color = Color(0xFF5FBDAC), // Default teal color dari gambar
    events: List<String> = emptyList(),
    bankAccounts: List<BankAccount> = emptyList(),
    maxHeight: androidx.compose.ui.unit.Dp? = null, // Ukuran maksimal tinggi card
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp, // Padding horizontal card
    verticalPadding: androidx.compose.ui.unit.Dp = 16.dp, // Padding vertikal card
    innerPadding: androidx.compose.ui.unit.Dp = 24.dp, // Padding dalam card
    avatarSize: androidx.compose.ui.unit.Dp = 100.dp, // Ukuran avatar
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp, // Radius sudut card
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
                // Avatar (Circular placeholder)
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(avatarColor)
                )

                // Action Buttons (Edit & Delete)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
private fun BankAccountRow(bankAccount: BankAccount) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bank Icon/Logo Placeholder
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