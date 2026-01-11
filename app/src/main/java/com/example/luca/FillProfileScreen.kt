package com.example.luca

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun FillProfileScreen() {
    // State untuk input username
    var username by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UIWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Menggunakan padding start 33dp sebagai anchor utama (x=33)
                .padding(start = 33.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            // --- 1. BACK ARROW ---
            // Posisi Y=57
            Spacer(modifier = Modifier.height(57.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = UIBlack,
                modifier = Modifier
                    .size(29.dp)
                    .clickable { /* TODO: Back Action */ }
            )

            // --- 2. TITLE ---
            // Posisi Y=190
            // Arrow (57) + Height (29) = 86.
            // Target 190. Gap = 104.dp
            Spacer(modifier = Modifier.height(104.dp))

            Text(
                text = "What’s your name?",
                style = AppFont.SemiBold,
                fontSize = 28.sp,
                color = UIBlack
            )

            // --- 3. SUBTITLE ---
            // Posisi Y=220
            // Title Y=190. Font 28sp visual height ~30-34dp.
            // Jaraknya sangat dekat, kita beri spacer 0 atau kecil.
            // Asumsi Spacer visual kecil agar pas di y=220 dari top title
            Text(
                text = "We want to know you more!",
                style = AppFont.Medium,
                fontSize = 14.sp,
                color = UIBlack.copy(alpha = 0.6f)
            )

            // --- 4. PROFILE PICTURE UPLOAD ---
            // Posisi Y Circle = 264
            // Subtitle Y=220 + Height (~20) = 240.
            // Target 264. Gap = 24.dp
            Spacer(modifier = Modifier.height(24.dp))

            // Container untuk area foto (Circle + Camera)
            Box(
                modifier = Modifier
                    // Global X=33. Target X=113.
                    // Padding start lokal = 113 - 33 = 80.dp
                    .padding(start = 80.dp)
                    .size(150.dp) // Dimensi 150x150
                    .clickable { /* TODO: Pick Image */ }
            ) {
                // Layer 1: Circle Background
                Image(
                    painter = painterResource(id = R.drawable.ic_circle_profile),
                    contentDescription = "Profile Placeholder",
                    modifier = Modifier.fillMaxSize()
                )

                // Layer 2: Camera Icon
                // Posisi Target Global: x=175.78, y=329
                // Posisi Parent Box Global: x=113, y=264
                // Offset Lokal X = 175.78 - 113 = 62.78.dp
                // Offset Lokal Y = 329 - 264 = 65.dp
                Image(
                    painter = painterResource(id = R.drawable.ic_camera_form),
                    contentDescription = "Upload Icon",
                    modifier = Modifier
                        .size(width = 24.44.dp, height = 20.dp)
                        .offset(x = 62.78.dp, y = 65.dp)
                )
            }

            // --- 5. LABEL "Profile Picture" ---
            // Posisi Y = 419
            // Circle Bottom = 264 + 150 = 414.
            // Target 419. Gap = 5.dp
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Profile Picture",
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack,
                // Target X=141. Global X=33.
                // Padding Start = 108.dp
                modifier = Modifier.padding(start = 108.dp)
            )

            // --- 6. USERNAME FORM ---
            // Posisi Y = 454
            // Label Y=419 + Height(~20) = 439.
            // Target 454. Gap = 15.dp
            Spacer(modifier = Modifier.height(15.dp))

            ProfileInputForm(
                text = username,
                onValueChange = { username = it },
                placeholder = "Username",
                iconRes = R.drawable.ic_user_form
            )

            // --- 7. BUTTON CREATE ACCOUNT ---
            // Posisi Y = 533
            // Form Bottom = 454 + 50 = 504.
            // Target 533. Gap = 29.dp
            Spacer(modifier = Modifier.height(29.dp))

            Button(
                onClick = { /* TODO: Create Account Action */ },
                modifier = Modifier
                    .size(308.dp, 50.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UIAccentYellow,
                    contentColor = UIBlack
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                // Button Text x=157 global.
                // Lebar button 308. Center ~154.
                // Text centered secara otomatis.
                Text(
                    text = "Create Account",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = UIBlack,
                    textAlign = TextAlign.Center
                )
            }

            // --- 8. DISCLAIMER TEXT ---
            // Posisi Y = 592
            // Button Bottom = 533 + 50 = 583.
            // Target 592. Gap = 9.dp
            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "*Don’t worry, you can change them later!",
                style = AppFont.Medium,
                fontSize = 12.sp,
                color = UIBlack.copy(alpha = 0.6f), // Agak pudar untuk note
                // Target X=76. Global X=33.
                // Padding Start = 43.dp
                modifier = Modifier.padding(start = 43.dp)
            )

            // --- 9. FOOTER ---
            // Posisi Y = 760
            // Disclaimer Y=592 + Height(~15) = 607.
            // Target 760. Gap = 153.dp
            Spacer(modifier = Modifier.height(153.dp))

            Text(
                text = "Privacy Policy   ·   Terms of Service",
                style = AppFont.SemiBold,
                fontSize = 12.sp,
                color = UIBlack.copy(alpha = 0.6f),
                // Target X=94. Global X=33.
                // Padding Start = 61.dp
                modifier = Modifier.padding(start = 61.dp)
            )
        }
    }
}

// --- KOMPONEN INPUT KHUSUS HALAMAN INI ---
@Composable
fun ProfileInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int
) {
    Box(
        modifier = Modifier
            .width(308.dp)
            .height(50.dp)
            .background(color = UIGrey, shape = RoundedCornerShape(23.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Icon User
            // Prompt: Icon x=62. Form Start x=33.
            // Selisih (Padding Start) = 29.dp
            Spacer(modifier = Modifier.width(29.dp))

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = UIBlack,
                modifier = Modifier.size(width = 11.dp, height = 13.dp) // Dimensi 11x13
            )

            // 2. Input Field
            // Prompt: Text x=87.
            // Icon x=62 + Lebar 11 = 73.
            // Selisih (Spacer) = 87 - 73 = 14.dp
            Spacer(modifier = Modifier.width(14.dp))

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
                    singleLine = true
                )
            }
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun FillProfileScreenPreview() {
    LucaTheme {
        FillProfileScreen()
    }
}