package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onAccountSettingsClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = AppFont.Bold,
                        fontSize = 20.sp,
                        color = UIBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = UIBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UIWhite,
                    scrolledContainerColor = UIWhite
                )
            )
        },
        containerColor = UIBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {

            // ===== 1. PROFILE HEADER SECTION =====
            // Bagian ini menampilkan ringkasan profil user
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(
                        color = UIWhite,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(UIGrey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = UIDarkGrey,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Jeremy Emmanuel", // Nanti diganti data dinamis
                    style = AppFont.Bold,
                    fontSize = 20.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "jeremy@example.com",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Edit Profile Kecil
                Button(
                    onClick = { onAccountSettingsClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UIAccentYellow,
                        contentColor = UIBlack
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Edit Profile", style = AppFont.SemiBold, fontSize = 12.sp)
                }
            }

            // ===== 2. GROUP: ACCOUNT SETTINGS =====
            SettingsGroupContainer(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy & Security",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "On, Email & Push",
                    onClick = { /* TODO */ }
                )
            }

            // ===== 3. GROUP: PREFERENCES =====
            SettingsGroupContainer(title = "Preferences") {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "English (US)",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = "System Default",
                    onClick = { /* TODO */ }
                )
            }

            // ===== 4. GROUP: SUPPORT & ABOUT =====
            SettingsGroupContainer(title = "Support") {
                SettingsItem(
                    icon = Icons.Default.QuestionAnswer,
                    title = "Help Center",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Default.Info, // Menggunakan icon Info yang ada di AboutUsScreen
                    title = "About Luca",
                    onClick = { onAboutUsClick() }
                )
            }

            // ===== 5. LOGOUT BUTTON =====
            // Logout biasanya dipisah atau diberi warna beda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { /* TODO: Logout Logic */ }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color.Red, // Warna merah untuk aksi destruktif
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Log Out",
                        style = AppFont.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                }
            }

            // Version info di paling bawah
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Version 1.0.0 (Build 102)",
                style = AppFont.Regular,
                fontSize = 12.sp,
                color = UIDarkGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
        }
    }
}

// ==========================================
// HELPER COMPOSABLES (REUSABLE UI COMPONENTS)
// ==========================================

@Composable
private fun SettingsGroupContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp) // Jarak antar grup
            .background(
                color = UIWhite,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp) // Padding dalam card
    ) {
        Text(
            text = title,
            style = AppFont.Bold,
            fontSize = 16.sp,
            color = UIAccentYellow, // Menggunakan warna aksen agar beda
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp), // Area klik yang nyaman
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box (Lingkaran kecil abu-abu)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(UIGrey.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UIBlack,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title & Subtitle
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = AppFont.SemiBold,
                fontSize = 15.sp,
                color = UIBlack
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey
                )
            }
        }

        // Arrow Icon (Indikator bisa diklik)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = UIDarkGrey,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview
@Composable
fun SettingsPreview() {
    LucaTheme {
        SettingsScreen(onBackClick = {}, onAboutUsClick = {}, onAccountSettingsClick = {})
    }
}