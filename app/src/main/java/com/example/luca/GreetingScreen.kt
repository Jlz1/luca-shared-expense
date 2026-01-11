package com.example.luca

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Pastikan import ini sesuai dengan nama package project kamu
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIWhite

@Composable
fun GreetingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UIWhite
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // --- 1. LOGO SECTION ---
            // Target Y: 118
            Spacer(modifier = Modifier.height(118.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_luca_logo),
                contentDescription = "Luca Logo",
                modifier = Modifier.size(width = 60.04.dp, height = 59.16.dp)
            )

            // --- 2. TEXT SECTION ---
            // Target Y Title: 187
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Welcome to Luca!",
                style = AppFont.SemiBold,
                fontSize = 28.sp,
                color = UIBlack,
                textAlign = TextAlign.Center
            )

            // Target Y Subtitle: 221
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Splitting bills made easy.",
                style = AppFont.Medium,
                fontSize = 16.sp,
                color = UIDarkGrey,
                textAlign = TextAlign.Center
            )

            // --- 3. SOCIAL BUTTONS SECTION ---
            // Target Y Google: 315
            // Subtitle ada di y=221 + tinggi text (~20) = 241
            // 315 - 241 = 74. Kita bulatkan 70.dp agar aman
            Spacer(modifier = Modifier.height(70.dp))

            // Google
            SocialButton(
                text = "Continue with Google",
                iconRes = R.drawable.ic_google_logo,
                onClick = {}
            )

            // Jarak antar button 16dp
            Spacer(modifier = Modifier.height(16.dp))

            // Facebook
            SocialButton(
                text = "Continue with Facebook",
                iconRes = R.drawable.ic_facebook_logo,
                onClick = {}
            )

            // Jarak antar button 16dp
            Spacer(modifier = Modifier.height(16.dp))

            // X (Twitter)
            SocialButton(
                text = "Continue with X",
                iconRes = R.drawable.ic_x_logo,
                onClick = {}
            )

            // --- 4. SIGN UP & LOG IN SECTION ---
            // Tombol X selesai di Y = 447 + 50 = 497
            // Tombol Sign Up mulai di Y = 604
            // Gap = 107.dp
            Spacer(modifier = Modifier.height(107.dp))

            // Sign Up (Filled Yellow)
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.size(325.dp, 50.dp),
                shape = RoundedCornerShape(49.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign Up",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Gap antar button
            Spacer(modifier = Modifier.height(16.dp))

            // Log In (Outlined Yellow)
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.size(325.dp, 50.dp),
                shape = RoundedCornerShape(49.dp),
                border = BorderStroke(1.dp, UIAccentYellow), // Stroke Kuning
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Log in",
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // --- 5. FOOTER SECTION ---
            // Log In selesai di Y = 670 + 50 = 720
            // Footer text mulai di Y = 760
            // Gap = 40.dp
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Privacy Policy   ·   Terms of Service",
                style = AppFont.SemiBold, // Atau AppFont.Medium jika terlalu tebal
                fontSize = 12.sp,
                color = UIBlack.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            // Spacer bawah untuk safety
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- COMPONENT REUSABLE UNTUK SOCIAL BUTTON ---
@Composable
fun SocialButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(325.dp, 50.dp),
        shape = RoundedCornerShape(49.dp),
        // Menggunakan warna abu-abu muda untuk stroke agar terlihat di background putih
        // Jika pakai 10% White di atas White, tidak akan terlihat.
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = UIWhite
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Padding start 14dp agar icon berada di posisi x=39 (25 + 14)
            Spacer(modifier = Modifier.width(14.dp))

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )

            Text(
                text = text,
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack,
                modifier = Modifier.weight(1f), // Dorong text ke tengah
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            // Dummy spacer di kanan (Width icon + Padding kiri) agar text benar-benar visual center
            Spacer(modifier = Modifier.width(39.dp))
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
//@Composable
//fun GreetingScreenPreview() {
//    LucaTheme {
//        GreetingScreen()
//    }
//}

@Composable
fun GreetingScreenPreview() {
    LucaTheme {
        GreetingScreen()
    }
}