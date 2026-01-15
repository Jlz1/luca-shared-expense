package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.UIBlack

@Composable
fun SidebarContent(
    onCloseClick: () -> Unit = {} // Placeholder untuk aksi tutup sidebar
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp) // Padding keseluruhan
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
        SidebarMenuItem(icon = Icons.Outlined.Home, text = "Dashboard") { /* Placeholder Click */ }
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
            icon = Icons.Outlined.HelpOutline,
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

@Preview(showBackground = true, widthDp = 320, heightDp = 800)
@Composable
fun PreviewSidebar() {
    SidebarContent()
}