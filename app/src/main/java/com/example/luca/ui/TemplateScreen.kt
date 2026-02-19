package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBackground
// Pastikan import HeaderSection ada
// import com.example.luca.ui.components.HeaderSection

@Composable
fun TemplateScreen(
    onMenuClick: () -> Unit = {}
) {
    // 1. HAPUS SCAFFOLD
    // Gunakan Column sebagai container utama
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIBackground) // Gunakan warna background tema
            .statusBarsPadding() // Penting: Agar konten tidak nabrak status bar HP
    ) {

        // 2. HEADER SECTION (Ditaruh manual di paling atas)
        HeaderSection(onLeftIconClick = onMenuClick)

        // 3. KONTEN UTAMA
        Box(
            modifier = Modifier
                .weight(1f) // Mengisi seluruh sisa ruang vertikal yang ada
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "New Event Screen")
        }

        // 4. SPACER BAWAH
        // Wajib ada supaya konten paling bawah tidak tertutup Navbar Global milik MainActivity
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateScreenPreview(){
    LucaTheme {
        TemplateScreen()
    }
}