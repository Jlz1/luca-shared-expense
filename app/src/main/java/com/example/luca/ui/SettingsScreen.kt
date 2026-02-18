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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.CoroutineScope
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
    onPasswordChanged: (String) -> Unit // Callback ketika password berhasil diubah
) {
    // Step 1: Verify current password, Step 2: Enter new password
    var currentStep by remember { mutableStateOf(1) }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isVerifying && !isUpdating) onDismiss() },
        containerColor = UIWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (currentStep == 1) "Verifikasi Password" else "Password Baru",
                style = AppFont.Bold,
                fontSize = 20.sp,
                color = UIBlack
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (currentStep == 1) {
                    // Step 1: Verify current password
                    Text(
                        text = "Masukkan password saat ini untuk melanjutkan",
                        fontSize = 14.sp,
                        style = AppFont.Regular,
                        color = UIDarkGrey,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it; errorMessage = "" },
                        label = { Text("Password Saat Ini") },
                        visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        enabled = !isVerifying,
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
                } else {
                    // Step 2: Enter new password
                    Text(
                        text = "Password terverifikasi! Masukkan password baru Anda",
                        fontSize = 14.sp,
                        style = AppFont.Regular,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMessage = "" },
                        label = { Text("Password Baru") },
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        enabled = !isUpdating,
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
                        label = { Text("Konfirmasi Password Baru") },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        enabled = !isUpdating,
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
                }

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
                    if (currentStep == 1) {
                        // Step 1: Verify old password
                        if (oldPassword.isEmpty()) {
                            errorMessage = "Password tidak boleh kosong"
                            return@Button
                        }

                        isVerifying = true
                        errorMessage = ""

                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null && user.email != null) {
                            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                                user.email!!,
                                oldPassword
                            )

                            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                                isVerifying = false
                                if (reauthTask.isSuccessful) {
                                    // Password correct, go to step 2
                                    currentStep = 2
                                    errorMessage = ""
                                } else {
                                    // Password wrong
                                    errorMessage = when {
                                        reauthTask.exception?.message?.contains("password", ignoreCase = true) == true ->
                                            "Password salah. Silakan coba lagi."
                                        reauthTask.exception?.message?.contains("network", ignoreCase = true) == true ->
                                            "Tidak ada koneksi internet"
                                        else -> "Password salah. Silakan coba lagi."
                                    }
                                }
                            }
                        } else {
                            isVerifying = false
                            errorMessage = "User tidak ditemukan. Silakan login kembali."
                        }
                    } else {
                        // Step 2: Update to new password
                        when {
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
                                    // Password valid, update di Firebase
                                    isUpdating = true
                                    errorMessage = ""

                                    val user = FirebaseAuth.getInstance().currentUser
                                    user?.updatePassword(newPassword)?.addOnCompleteListener { updateTask ->
                                        isUpdating = false
                                        if (updateTask.isSuccessful) {
                                            onPasswordChanged("Password berhasil diubah")
                                            onDismiss()
                                        } else {
                                            errorMessage = "Gagal mengubah password: ${updateTask.exception?.message}"
                                        }
                                    } ?: run {
                                        isUpdating = false
                                        errorMessage = "User tidak ditemukan"
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !isVerifying && !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isVerifying || isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = UIBlack,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (currentStep == 1) "Verifikasi" else "Ubah Password",
                        color = UIBlack,
                        style = AppFont.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (currentStep == 2) {
                        // Go back to step 1
                        currentStep = 1
                        newPassword = ""
                        confirmPassword = ""
                        errorMessage = ""
                    } else {
                        onDismiss()
                    }
                },
                enabled = !isVerifying && !isUpdating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (currentStep == 2) "Kembali" else "Batal",
                    color = UIDarkGrey,
                    style = AppFont.Medium
                )
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
    onPrivacySecurityClick: () -> Unit = {},
    onHelpCenterClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // State untuk user profile
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("") }
    var userAvatarName by remember { mutableStateOf("avatar_1") }

    // State untuk password change dan success message
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val debouncedBackClick = debounceBackClick(scope) { onBackClick() }

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
                    IconButton(onClick = debouncedBackClick) {
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
                // Notifications setting removed because it's no longer used
            }

            // ===== 3. GROUP: SUPPORT & ABOUT =====
            SettingsGroupContainer(title = "Support") {
                SettingsItem(
                    icon = Icons.Default.QuestionAnswer,
                    title = "Help Center",
                    onClick = { onHelpCenterClick() }
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
                    .clickable { showLogoutConfirmDialog = true }
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
            onPasswordChanged = { message ->
                successMessage = message
                showSuccessMessage = true
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = UIWhite,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(UIAccentYellow.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = UIAccentYellow,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Keluar Akun?",
                    style = AppFont.Bold,
                    fontSize = 20.sp,
                    color = UIBlack,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kamu harus login ulang untuk mengakses data Luca.",
                        color = UIDarkGrey,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showLogoutConfirmDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UIGrey,
                                contentColor = UIBlack
                            ),
                            shape = RoundedCornerShape(50.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            modifier = Modifier.height(48.dp).weight(1f)
                        ) {
                            Text(
                                text = "No",
                                style = AppFont.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                showLogoutConfirmDialog = false
                                onLogoutClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UIAccentYellow,
                                contentColor = UIBlack
                            ),
                            shape = RoundedCornerShape(50),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            modifier = Modifier.height(48.dp).weight(1f)
                        ) {
                            Text(
                                text = "Yes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
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