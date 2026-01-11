package com.example.luca

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIWhite

@Composable
fun FinalSignUpScreen(
    name: String = "Benita" // Default value untuk preview
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = UIWhite
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- LAYER 1: BACKGROUND IMAGE ---
            // Mengisi seluruh layar (atau bagian atas sesuai ukuran gambar asli)
            Image(
                painter = painterResource(id = R.drawable.bg_accent_final_page),
                contentDescription = "Background Pattern",
                modifier = Modifier.fillMaxSize(), // Atau .fillMaxWidth() jika height otomatis
                contentScale = ContentScale.Crop // Agar gambar mengisi penuh tanpa distorsi aneh
            )

            // --- LAYER 2: KONTEN ---
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 1. Title "Welcome Back!"
                // Target Y = 227
                Spacer(modifier = Modifier.height(227.dp))

                Text(
                    text = "Welcome to the Crew!",
                    style = AppFont.Bold, // Bold sesuai request
                    fontSize = 28.sp,
                    color = UIBlack
                )

                // 2. Profile Circle
                // Target Y Circle = 284
                // Posisi Text Y=227. Estimasi tinggi text ~35dp.
                // Selisih Top Text ke Top Circle = 284 - 227 = 57dp.
                // Jadi Spacer = 57 - TinggiText. Kita pakai estimasi 20-22dp.
                Spacer(modifier = Modifier.height(20.dp))

                // Placeholder Foto Profil (Lingkaran)
                Image(
                    // Ganti dengan foto profil user nantinya (bitmap/url)
                    // Untuk sekarang saya pakai gambar sample atau placeholder
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Ganti resource foto monyet jika ada
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(212.dp) // Dimensi 212x212
                        .clip(CircleShape) // Memotong jadi lingkaran
                        .background(Color.Gray) // Background sementara kalau gambar tidak load
                )

                // 3. Nama User
                // Target Y Nama = 519
                // Circle Bottom = 284 (Start) + 212 (Height) = 496.
                // Target 519. Selisih = 23dp.
                Spacer(modifier = Modifier.height(23.dp))

                Text(
                    text = name, // Mengambil dari parameter
                    style = AppFont.SemiBold,
                    fontSize = 28.sp,
                    color = UIBlack
                )
            }
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun FinalSignUpScreenPreview() {
    LucaTheme {
        FinalSignUpScreen(name = "Benita")
    }
}