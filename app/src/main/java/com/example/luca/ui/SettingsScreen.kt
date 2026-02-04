package com.example.luca.ui

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.ui.theme.*
import com.example.luca.util.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ==========================================
// HELPER COMPOSABLES
// ==========================================

@Composable
fun SettingsGroupContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = AppFont.Bold,
            fontSize = 16.sp,
            color = UIAccentYellow,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = AppFont.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PasswordChangeDialogComponent(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UIWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Change Password",
                style = AppFont.Bold,
                fontSize = 20.sp,
                color = UIBlack
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter your current password and new password",
                    fontSize = 14.sp,
                    style = AppFont.Regular,
                    color = UIDarkGrey,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it; errorMessage = "" },
                    label = { Text("Current Password") },
                    visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOldPassword = !showOldPassword }) {
                            Icon(
                                imageVector = if (showOldPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = "" },
                    label = { Text("New Password") },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = "" },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        style = AppFont.Regular,
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        oldPassword.isEmpty() -> {
                            errorMessage = "Password saat ini tidak boleh kosong"
                        }
                        newPassword.isEmpty() -> {
                            errorMessage = "Password baru tidak boleh kosong"
                        }
                        confirmPassword.isEmpty() -> {
                            errorMessage = "Konfirmasi password tidak boleh kosong"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Password baru tidak cocok"
                        }
                        else -> {
                            // Validasi password baru menggunakan ValidationUtils
                            val passwordError = ValidationUtils.getPasswordError(newPassword)
                            if (passwordError != null) {
                                errorMessage = passwordError
                            } else {
                                // Password valid, proceed
                                onConfirm(oldPassword, newPassword)
                                onDismiss()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Change Password", color = UIBlack, style = AppFont.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = UIDarkGrey, style = AppFont.Medium)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onPrivacySecurityClick: () -> Unit = {}
) {
    // State untuk user profile
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("") }
    var userAvatarName by remember { mutableStateOf("avatar_1") }

    // State untuk password change dan success message
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // Load user profile dari Firebase
    LaunchedEffect(Unit) {
        try {
            val repository = LucaFirebaseRepository()
            val userContact = withContext(Dispatchers.IO) {
                repository.getCurrentUserAsContact()
            }
            if (userContact != null) {
                userName = userContact.name
                userAvatarName = userContact.avatarName
                // Try to get email dari Firebase Auth
                userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "No email"
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsScreen", "Failed to load user profile: ${e.message}")
            userName = "User"
            userEmail = "error loading email"
        }
    }

    // Auto-hide success message setelah 3 detik
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(3000)
            showSuccessMessage = false
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
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
                // Avatar profile dengan gambar atau fallback ke icon
                val resourceId = remember(userAvatarName) {
                    if (userAvatarName.isNotBlank()) {
                        try {
                            val rClass = Class.forName("com.example.luca.R\$drawable")
                            val field = rClass.getField(userAvatarName)
                            field.getInt(null)
                        } catch (_: Exception) {
                            0
                        }
                    } else {
                        0
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(UIGrey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (resourceId != 0) {
                        // Load avatar dari database
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback ke icon jika avatar tidak tersedia
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = UIDarkGrey,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // User name dari Firebase
                Text(
                    text = userName,
                    style = AppFont.Bold,
                    fontSize = 20.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(4.dp))

                // User email dari Firebase Auth
                Text(
                    text = userEmail,
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
                    onClick = { showPasswordChangeDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy & Security",
                    onClick = { onPrivacySecurityClick() }
                )
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "On, Email & Push",
                    onClick = { /* TODO */ }
                )
            }

            // ===== 3. GROUP: SUPPORT & ABOUT =====
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
                        imageVector = Icons.AutoMirrored.Filled.Logout,
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

        // Success Message Display (inside Scaffold Box)
        if (showSuccessMessage) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
                    .background(
                        if (successMessage.contains("berhasil", ignoreCase = true) ||
                            successMessage.contains("successfully", ignoreCase = true))
                            Color(0xFF4CAF50).copy(alpha = 0.9f)
                        else
                            Color(0xFFE53935).copy(alpha = 0.9f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (successMessage.contains("berhasil", ignoreCase = true) ||
                            successMessage.contains("successfully", ignoreCase = true))
                            Icons.Default.Check
                        else
                            Icons.Default.Close,
                        contentDescription = if (successMessage.contains("berhasil", ignoreCase = true) ||
                            successMessage.contains("successfully", ignoreCase = true)) "Success" else "Error",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        successMessage,
                        color = Color.White,
                        style = AppFont.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    } // Scaffold closing
    } // Outer Box closing

    // Password Change Dialog
    if (showPasswordChangeDialog) {
        PasswordChangeDialogComponent(
            onDismiss = { showPasswordChangeDialog = false },
            onConfirm = { oldPassword: String, newPassword: String ->
                scope.launch {
                    try {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null && user.email != null) {
                            // Reauthenticate user dengan old password untuk memastikan password lama benar
                            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                                user.email!!,
                                oldPassword
                            )

                            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                                if (reauthTask.isSuccessful) {
                                    // Password lama benar, sekarang update password baru
                                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            successMessage = "Password berhasil diubah"
                                            showSuccessMessage = true
                                            android.util.Log.d("SettingsScreen", "✅ Password changed successfully")
                                        } else {
                                            // Error saat update password
                                            val errorMsg = when (updateTask.exception?.message) {
                                                null -> "Gagal mengubah password"
                                                else -> "Gagal mengubah password: ${updateTask.exception?.message}"
                                            }
                                            successMessage = errorMsg
                                            showSuccessMessage = true
                                            android.util.Log.e("SettingsScreen", "❌ Failed to update password: ${updateTask.exception?.message}")
                                        }
                                    }
                                } else {
                                    // Password lama salah
                                    val errorMsg = when {
                                        reauthTask.exception?.message?.contains("password", ignoreCase = true) == true ->
                                            "Password saat ini salah"
                                        reauthTask.exception?.message?.contains("network", ignoreCase = true) == true ->
                                            "Tidak ada koneksi internet"
                                        else -> "Password saat ini salah"
                                    }
                                    successMessage = errorMsg
                                    showSuccessMessage = true
                                    android.util.Log.e("SettingsScreen", "❌ Reauthentication failed: ${reauthTask.exception?.message}")
                                }
                            }
                        } else {
                            successMessage = "User tidak ditemukan. Silakan login kembali"
                            showSuccessMessage = true
                            android.util.Log.e("SettingsScreen", "❌ User or email is null")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SettingsScreen", "❌ Error: ${e.message}")
                        successMessage = "Terjadi kesalahan: ${e.message}"
                        showSuccessMessage = true
                    }
                }
                showPasswordChangeDialog = false
            }
        )
    }
}

@Preview
@Composable
fun SettingsPreview() {
    LucaTheme {
        SettingsScreen(
            onBackClick = {},
            onAboutUsClick = {},
            onAccountSettingsClick = {},
            onPrivacySecurityClick = {}
        )
    }
}