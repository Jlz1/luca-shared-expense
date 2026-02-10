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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.luca.R
import com.example.luca.data.AuthRepository
// Import Component Overlay yang baru dibuat
import com.example.luca.ui.components.AvatarSelectionOverlay
import com.example.luca.ui.theme.*
// Import Utils yang baru dibuat
import com.example.luca.util.AvatarUtils
import com.example.luca.ui.debounceBackClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillProfileScreen(
    onBackClick: () -> Unit,
    onCreateAccountClick: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }

    // Default Avatar (Kosong, tampilkan placeholder abu-abu dengan icon camera)
    var selectedAvatarName by remember { mutableStateOf("") }

    // State untuk kontrol muncul/tidaknya overlay dialog
    var showAvatarDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository() }
    var isLoading by remember { mutableStateOf(false) }

    val debouncedBackClick = debounceBackClick(scope) { onBackClick() }

    val handleCreateAccount: () -> Unit = {
        if (username.isNotEmpty()) {
            isLoading = true
            scope.launch {
                // UPDATE: Kirim String AvatarName, bukan URI
                val result = authRepo.updateProfile(username, selectedAvatarName)
                isLoading = false

                result.onSuccess {
                    Toast.makeText(context, "Profile Disimpan!", Toast.LENGTH_SHORT).show()
                    onCreateAccountClick(username, selectedAvatarName)
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
        onUsernameChange = { username = it },
        selectedAvatarName = selectedAvatarName,
        showAvatarDialog = showAvatarDialog,
        onShowAvatarDialog = { showAvatarDialog = true },
        onDismissAvatarDialog = { showAvatarDialog = false },
        onAvatarSelected = { selectedAvatarName = it },
        isLoading = isLoading,
        onBackClick = debouncedBackClick,
        onCreateAccountClick = handleCreateAccount
    )
}

// --- STATELESS COMPOSABLE (Pure UI) ---
@Composable
fun FillProfileScreenContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    selectedAvatarName: String,
    showAvatarDialog: Boolean,
    onShowAvatarDialog: () -> Unit,
    onDismissAvatarDialog: () -> Unit,
    onAvatarSelected: (String) -> Unit,
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

                    // --- AREA FOTO PROFIL (KLIK UNTUK GANTI) ---
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(UIGrey)
                            .clickable { onShowAvatarDialog() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatarName.isNotEmpty()) {
                            // Tampilkan avatar yang dipilih
                            Image(
                                painter = painterResource(id = AvatarUtils.getAvatarResId(selectedAvatarName)),
                                contentDescription = "Selected Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Tampilkan icon camera saat belum pilih avatar
                            Image(
                                painter = painterResource(id = R.drawable.ic_camera_form),
                                contentDescription = "Edit",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "Tap to change avatar",
                        style = AppFont.Medium,
                        fontSize = 12.sp,
                        color = UIAccentYellow
                    )

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

            // --- PANGGIL OVERLAY DI SINI ---
            if (showAvatarDialog) {
                AvatarSelectionOverlay(
                    currentSelection = selectedAvatarName,
                    onDismiss = onDismissAvatarDialog,
                    onAvatarSelected = { newName ->
                        onAvatarSelected(newName)
                    }
                )
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
            username = "",
            onUsernameChange = {},
            selectedAvatarName = "",
            showAvatarDialog = false,
            onShowAvatarDialog = {},
            onDismissAvatarDialog = {},
            onAvatarSelected = {},
            isLoading = false,
            onBackClick = {},
            onCreateAccountClick = {}
        )
    }
}