package com.example.luca.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.luca.FloatingNavbar
import com.example.luca.HeaderSection
import com.example.luca.ui.theme.LucaTheme

@Composable
fun TemplateScreen() {
    Scaffold(
        // 1. Header Tetap di Atas
        topBar = {
            HeaderSection() // Fungsi Header UiDark kamu
        },

        // 2. Navbar Melayang ditaruh di slot FAB agar tidak memotong list
        floatingActionButton = {
            FloatingNavbar()
        },
        // Atur posisi Navbar ke tengah bawah
        floatingActionButtonPosition = FabPosition.Center,

        // Memastikan konten bisa 'tembus' ke area bawah layar
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // Tambahkan padding ini agar konten mulai di bawah Header
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding), // PENTING!
            contentAlignment = Alignment.Center
        ) {
            Text(text = "New Event Screen")
        }
    }
}

@Preview
@Composable
fun TestPreview(){
    LucaTheme {
        TemplateScreen()
    }
}