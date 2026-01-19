package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIWhite
import kotlinx.coroutines.delay

@Composable
fun FinalScreen(
    name: String = "Benita",
    onNavigateToHome: () -> Unit = {}
) {
    // Delay 1.5 detik lalu navigasi ke Home
    LaunchedEffect(Unit) {
        delay(1500L)
        onNavigateToHome()
    }

    Box(modifier = Modifier.fillMaxSize().background(UIWhite), contentAlignment = Alignment.Center) {

        // --- LAYER 1: BACKGROUND IMAGE ---
        Image(
            painter = painterResource(id = R.drawable.bg_accent_final_page),
            contentDescription = "Background Pattern",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- LAYER 2: KONTEN ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back!",
                style = AppFont.Bold,
                fontSize = 28.sp,
                color = UIBlack
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(212.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(23.dp))

            Text(
                text = name,
                style = AppFont.SemiBold,
                fontSize = 28.sp,
                color = UIBlack
            )
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun FinalScreenPreview() {
    LucaTheme {
        FinalScreen(name = "Benita")
    }
}