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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.luca.R
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun FinalScreen(
    onNavigateToHome: () -> Unit = {}
) {
    // State untuk menyimpan data user
    var name by remember { mutableStateOf("User") }
    var avatarName by remember { mutableStateOf("") }
    var isDataLoaded by remember { mutableStateOf(false) }

    // Ambil data user dari Firestore lalu navigasi ke Home
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                name = doc.getString("username") ?: currentUser.displayName ?: "User"
                avatarName = doc.getString("avatarName") ?: ""
            } catch (e: Exception) {
                name = currentUser.displayName ?: "User"
                avatarName = ""
            }
        }

        // Tandai data sudah siap ditampilkan
        isDataLoaded = true

        // Tahan sebentar biar user bisa lihat screen "Welcome Back", baru pindah
        delay(2000L)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UIWhite),
        contentAlignment = Alignment.Center
    ) {

        // --- LAYER 1: BACKGROUND IMAGE ---
        // Pastikan drawable bg_accent_final_page ada di folder res/drawable
        Image(
            painter = painterResource(id = R.drawable.bg_accent_final_page),
            contentDescription = "Background Pattern",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- LAYER 2: KONTEN ---
        // Hanya tampilkan jika data sudah selesai diload
        if (isDataLoaded) {
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

                // --- AVATAR MENGGUNAKAN DICEBEAR (COIL) ---
                val avatarUrl = remember(avatarName) {
                    // Jika avatarName kosong, gunakan name sebagai seed, atau default "User"
                    val seed = if (avatarName.isNotEmpty()) avatarName else name
                    "https://api.dicebear.com/9.x/avataaars/png?seed=${seed}"
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(212.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray), // Background sementara saat loading
                    placeholder = painterResource(R.drawable.ic_launcher_foreground), // Ganti dengan icon user kamu jika ada
                    error = painterResource(R.drawable.ic_launcher_foreground)
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
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun FinalScreenPreview() {
    LucaTheme {
        FinalScreen()
    }
}