package com.example.luca

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
// import androidx.compose.material3.Text <-- Kita hapus ini biar tidak bingung, kita panggil langsung di bawah
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- DATA CLASS ---
data class UserData(
    val name: String,
    val isCurrentUser: Boolean = false,
    val avatarColor: Color? = null
)

// --- MAIN COMPONENT ---
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

// --- SUB COMPONENTS ---

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
            androidx.compose.material3.Text(
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
            androidx.compose.material3.Text(
                text = "",
                fontSize = calculateFontSize(avatarSize),
                maxLines = 1
            )
        }
    }
}

// --- HELPER ---
private fun calculateFontSize(avatarSize: Dp): androidx.compose.ui.unit.TextUnit {
    return (avatarSize.value * 0.25).sp
}

private fun getRandomAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
        Color(0xFFFFA07A), Color(0xFF98D8C8), Color(0xFFF7B731),
        Color(0xFFEE5A6F), Color(0xFF786FA6), Color(0xFFEA8685),
        Color(0xFF63CDDA)
    )
    val index = kotlin.math.abs(name.hashCode()) % colors.size
    return colors[index]
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun AvatarListPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        // Preview dengan androidx.compose.material3.Text juga
        androidx.compose.material3.Text("Preview: With Add Button")
        Spacer(modifier = Modifier.height(10.dp))

        // Kalau mau pake copas saja dari sini
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
        // sampai sini teman teman
    }
}