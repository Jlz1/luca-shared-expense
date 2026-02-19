package com.example.luca.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.luca.R
import com.example.luca.data.AuthRepository
import com.example.luca.ui.theme.*

// --- UTILS Helper untuk URL ---
object AvatarUtils {
    fun getDiceBearUrl(seed: String): String {
        return "https://api.dicebear.com/9.x/avataaars/png?seed=$seed"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillProfileScreen(
    onBackClick: () -> Unit,
    onCreateAccountClick: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }

    // Avatar Seed (String acak untuk DiceBear)
    var selectedAvatarSeed by remember { mutableStateOf("") }

    // Counter untuk variasi saat dipencet
    var tapCount by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }
    var isLoading by remember { mutableStateOf(false) }

    // Guard navigasi back
    val backClicked = remember { mutableStateOf(false) }
    val handleBackClick: () -> Unit = {
        if (!backClicked.value) {
            backClicked.value = true
            onBackClick()
        }
    }

    // LOGIKA UTAMA: Ganti Avatar saat diklik
    val handleAvatarTap: () -> Unit = {
        // Ambil nama dasar, kalau kosong pakai "User"
        val baseName = username.ifEmpty { "User" }

        // Naikkan counter (0 -> 1 -> 2...)
        tapCount++

        // Bikin seed baru: "Jeremy1", "Jeremy2", dst
        selectedAvatarSeed = "$baseName$tapCount"
    }

    val handleCreateAccount: () -> Unit = {
        if (username.isNotEmpty()) {
            isLoading = true
            scope.launch {
                // Tentukan seed final yang akan disimpan
                val finalSeed = if (selectedAvatarSeed.isNotEmpty()) selectedAvatarSeed else username

                // Simpan username dan SEED ke database
                val result = authRepo.updateProfile(username, finalSeed)
                isLoading = false

                result.onSuccess {
                    Toast.makeText(context, "Profile Disimpan!", Toast.LENGTH_SHORT).show()
                    onCreateAccountClick(username, finalSeed)
                }

                result.onFailure { exception ->
                    val pesanError = exception.message ?: "Gagal simpan profile"
                    Toast.makeText(context, "Error: $pesanError", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "Username wajib diisi!", Toast.LENGTH_SHORT).show()
        }
    }

    FillProfileScreenContent(
        username = username,
        onUsernameChange = { newName ->
            username = newName
            // Opsional: Kalau mau avatar berubah saat ngetik, bisa hapus baris tapCount reset
        },
        // Logic Display: Kalau belum pernah dipencet, tampilin sesuai username
        displaySeed = if (selectedAvatarSeed.isNotEmpty()) selectedAvatarSeed else (username.ifEmpty { "User" }),
        onAvatarTap = handleAvatarTap,
        isLoading = isLoading,
        onBackClick = handleBackClick,
        onCreateAccountClick = handleCreateAccount
    )
}

// --- STATELESS COMPOSABLE (Pure UI) ---
@Composable
fun FillProfileScreenContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    displaySeed: String, // Seed untuk URL DiceBear
    onAvatarTap: () -> Unit,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    Scaffold(
        containerColor = UIWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIWhite)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Back Icon
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = UIBlack,
                        modifier = Modifier
                            .size(29.dp)
                            .clickable { onBackClick() }
                    )
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    Text(
                        text = "What's your name?",
                        style = AppFont.SemiBold,
                        fontSize = 28.sp,
                        color = UIBlack
                    )
                    Text(
                        text = "We want to know you more!",
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        color = UIBlack.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // --- AREA DICEBEAR AVATAR (TAP TO SHUFFLE) ---
                    val avatarUrl = remember(displaySeed) { AvatarUtils.getDiceBearUrl(displaySeed) }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(UIGrey) // Background abu-abu
                            .clickable { onAvatarTap() } // AKSI KLIK DISINI
                    ) {
                        // Menggunakan SubcomposeAsyncImage agar bisa Loading Muter-muter
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Avatar Seed: $displaySeed",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),

                            // LOGIKA LOADING SPINNER
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(30.dp),
                                        color = UIBlack,
                                        strokeWidth = 3.dp
                                    )
                                }
                            },

                            // LOGIKA ERROR (Fallback Icon)
                            error = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher_foreground), // Pastikan icon ini ada
                                        contentDescription = "Error",
                                        modifier = Modifier.size(40.dp),
                                        alpha = 0.5f
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "Tap avatar to shuffle style",
                        style = AppFont.Medium,
                        fontSize = 12.sp,
                        color = UIAccentYellow
                    )

                    // Debug text (Boleh dihapus)
                    // Text(text = displaySeed, fontSize = 10.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(40.dp))

                    // Input Username
                    ProfileInputForm(
                        text = username,
                        onValueChange = onUsernameChange,
                        placeholder = "Username",
                        iconRes = R.drawable.ic_user_form,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Tombol Create
                    Button(
                        onClick = onCreateAccountClick,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow,
                            contentColor = UIBlack
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = UIBlack,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                style = AppFont.Medium,
                                fontSize = 14.sp,
                                color = UIBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONEN INPUT ---
@Composable
fun ProfileInputForm(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(color = UIGrey, shape = RoundedCornerShape(23.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.width(29.dp))
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = UIBlack,
                modifier = Modifier.size(width = 11.dp, height = 13.dp)
            )
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(device = "spec:width=375dp,height=812dp,dpi=440")
@Composable
fun FillProfileScreenPreview() {
    LucaTheme {
        FillProfileScreenContent(
            username = "Test",
            onUsernameChange = {},
            displaySeed = "Test",
            onAvatarTap = {},
            isLoading = false,
            onBackClick = {},
            onCreateAccountClick = {}
        )
    }
}