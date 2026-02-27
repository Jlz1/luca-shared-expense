package com.luca.shared.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.luca.shared.R
import com.luca.shared.ui.theme.*
import com.luca.shared.util.AvatarUtils
import com.luca.shared.util.ValidationUtils
import com.luca.shared.viewmodel.AccountSettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showProfilePictureDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialogGoogle by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsState()
    val username by viewModel.username.collectAsState()
    val selectedAvatarName by viewModel.selectedAvatarName.collectAsState()
    val isDataLoading by viewModel.isLoading.collectAsState()

    val scope = rememberCoroutineScope()

    // Guard untuk mencegah klik back berkali-kali yang menyebabkan bug navigation
    val backClicked = remember { mutableStateOf(false) }
    val handleBackClick: () -> Unit = {
        if (!backClicked.value) {
            backClicked.value = true
            onBackClick()
        }
    }


    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(3000)
            showSuccessMessage = false
        }
    }

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
            ) {
                // Header
                HeaderSection(
                    currentState = HeaderState.ACCOUNT_SETTINGS,
                    onLeftIconClick = handleBackClick
                )

                // Loading State - Tampilkan loading screen sampai data selesai di-load
                if (isDataLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(UIWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = UIAccentYellow,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading...",
                                style = AppFont.Medium,
                                fontSize = 16.sp,
                                color = UIDarkGrey
                            )
                        }
                    }
                } else {
                    // Scrollable Content - Hanya tampilkan setelah data loaded
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 20.dp)
                    ) {
                    // Profile Picture Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color(0xFFFF8C42)
                                    )
                                    .clickable(enabled = !isDataLoading) { showProfilePictureDialog = true }
                                    .border(3.dp, UIAccentYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarUrl = remember(selectedAvatarName) {
                                    "https://api.dicebear.com/9.x/avataaars/png?seed=${selectedAvatarName}"
                                }

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,

                                    // --- BAGIAN INI YANG DIGANTI ---
                                    // Ganti painterResource(...) dengan rememberVectorPainter(...)
                                    placeholder = rememberVectorPainter(Icons.Default.Person),
                                    error = rememberVectorPainter(Icons.Default.Person)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Edit Profile Picture",
                                fontSize = 14.sp,
                                style = AppFont.Medium,
                                color = UIDarkGrey
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Divider
                    HorizontalDivider(
                        color = UIGrey,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username Section
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = "Username",
                            fontSize = 16.sp,
                            style = AppFont.SemiBold,
                            color = UIBlack
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = UIGrey),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isDataLoading) { viewModel.setEditingUsername(true) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (isDataLoading) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = UIAccentYellow
                                            )
                                            Text(
                                                text = "Loading...",
                                                fontSize = 16.sp,
                                                style = AppFont.Medium,
                                                color = UIDarkGrey
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = username,
                                            fontSize = 16.sp,
                                            style = AppFont.Medium,
                                            color = UIBlack
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Username",
                                    tint = UIBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (viewModel.isEditingUsername.collectAsState().value) {
                            Spacer(modifier = Modifier.height(16.dp))
                            var newUsername by remember { mutableStateOf(username) }

                            CustomRoundedTextField(
                                value = newUsername,
                                onValueChange = { newUsername = it },
                                placeholder = "Enter new username",
                                backgroundColor = UIGrey
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.setEditingUsername(false)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Cancel",
                                        color = UIBlack,
                                        style = AppFont.Medium
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (newUsername.isNotBlank()) {
                                            scope.launch {
                                                isLoading = true
                                                viewModel.updateUsername(newUsername)
                                                isLoading = false
                                                successMessage = "Username updated successfully"
                                                showSuccessMessage = true
                                                viewModel.setEditingUsername(false)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Save",
                                        color = UIBlack,
                                        style = AppFont.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(
                        color = UIGrey,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Password Section - Only show for email/password users
                    if (!viewModel.isGoogleUser()) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "Password",
                                fontSize = 16.sp,
                                style = AppFont.SemiBold,
                                color = UIBlack
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = UIGrey),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPasswordDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "••••••••",
                                        fontSize = 16.sp,
                                        style = AppFont.Medium,
                                        color = UIBlack
                                    )
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Password",
                                        tint = UIBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(
                            color = UIGrey,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Delete Account & Logout Section
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Button(
                            onClick = {
                                // Check if user is Google user
                                if (viewModel.isGoogleUser()) {
                                    showDeleteConfirmDialogGoogle = true
                                } else {
                                    showDeleteConfirmDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935).copy(alpha = 0.1f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Delete Account",
                                color = Color(0xFFE53935),
                                style = AppFont.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showLogoutConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = UIBlack,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                "Logout",
                                color = UIBlack,
                                style = AppFont.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
                }
            }

            // Success Message
            if (showSuccessMessage) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth()
                        .background(
                            Color(0xFF4CAF50).copy(alpha = 0.9f),
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
                            Icons.Default.Check,
                            contentDescription = "Success",
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
        }
    }

    // Dialogs
    if (showProfilePictureDialog) {
        AvatarSelectionOverlay(
            currentSelection = selectedAvatarName,
            onAvatarSelected = { avatarName ->
                scope.launch {
                    isLoading = true
                    viewModel.updateAvatarName(avatarName)
                    isLoading = false
                    successMessage = "Profile picture updated"
                    showSuccessMessage = true
                    showProfilePictureDialog = false
                }
            },
            onDismiss = { showProfilePictureDialog = false }
        )
    }

    if (showPasswordDialog) {
        PasswordChangeDialog(
            onDismiss = { showPasswordDialog = false },
            onPasswordChanged = { message ->
                successMessage = message
                showSuccessMessage = true
            }
        )
    }


    if (showDeleteConfirmDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = { password ->
                scope.launch {
                    isLoading = true
                    val success = viewModel.deleteAccount(password)
                    isLoading = false
                    if (success) {
                        showDeleteConfirmDialog = false
                        onLogoutClick()
                    } else {
                        successMessage = "Invalid password"
                        showSuccessMessage = true
                    }
                }
            }
        )
    }

    if (showDeleteConfirmDialogGoogle) {
        DeleteAccountDialogGoogle(
            onDismiss = { showDeleteConfirmDialogGoogle = false },
            onConfirm = {
                scope.launch {
                    isLoading = true
                    val success = viewModel.deleteAccount(null)
                    isLoading = false
                    if (success) {
                        showDeleteConfirmDialogGoogle = false
                        onLogoutClick()
                    } else {
                        successMessage = "Failed to delete account"
                        showSuccessMessage = true
                    }
                }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = UIWhite,
            shape = RoundedCornerShape(24.dp),
            icon = { Box(modifier = Modifier.size(72.dp).background(UIAccentYellow.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = UIAccentYellow, modifier = Modifier.size(32.dp)) } },
            title = { Text(text = "Keluar Akun?", style = AppFont.Bold, fontSize = 20.sp, color = UIBlack, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Kamu harus login ulang untuk mengakses data Luca.", color = UIDarkGrey, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { showLogoutConfirmDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = UIGrey, contentColor = UIBlack), shape = RoundedCornerShape(50.dp), elevation = ButtonDefaults.buttonElevation(0.dp), modifier = Modifier.height(48.dp).weight(1f)) { Text(text = "No", style = AppFont.SemiBold, fontSize = 14.sp) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { showLogoutConfirmDialog = false; onLogoutClick() }, colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow, contentColor = UIBlack), shape = RoundedCornerShape(50), elevation = ButtonDefaults.buttonElevation(0.dp), modifier = Modifier.height(48.dp).weight(1f)) { Text(text = "Yes", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun PasswordChangeDialog(
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
                text = if (currentStep == 1) "Password Verification" else "New Password",
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
                        text = "Enter your current password to continue",
                        fontSize = 14.sp,
                        style = AppFont.Regular,
                        color = UIDarkGrey,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    CustomTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it; errorMessage = "" },
                        placeholder = "Current Password",
                        isPassword = true,
                        showPassword = showOldPassword,
                        onShowPasswordToggle = { showOldPassword = !showOldPassword }
                    )
                } else {
                    // Step 2: Enter new password
                    Text(
                        text = "Password verified! Enter your new password",
                        fontSize = 14.sp,
                        style = AppFont.Regular,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    CustomTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMessage = "" },
                        placeholder = "New Password",
                        isPassword = true,
                        showPassword = showNewPassword,
                        onShowPasswordToggle = { showNewPassword = !showNewPassword }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = "" },
                        placeholder = "Konfirmasi Password Baru",
                        isPassword = true,
                        showPassword = showConfirmPassword,
                        onShowPasswordToggle = { showConfirmPassword = !showConfirmPassword }
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFE53935),
                        fontSize = 12.sp,
                        style = AppFont.Regular
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
                            errorMessage = "Password cannot be blank"
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
                                            "Incorrect password. Please try again."
                                        reauthTask.exception?.message?.contains("network", ignoreCase = true) == true ->
                                            "No internet connection"
                                        else -> "Incorrect password. Please try again."
                                    }
                                }
                            }
                        } else {
                            isVerifying = false
                            errorMessage = "User not found. Please log in again."
                        }
                    } else {
                        // Step 2: Update to new password
                        when {
                            newPassword.isEmpty() -> {
                                errorMessage = "The new password cannot be blank."
                            }
                            confirmPassword.isEmpty() -> {
                                errorMessage = "The password confirmation cannot be left blank."
                            }
                            newPassword != confirmPassword -> {
                                errorMessage = "The new password does not match"
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
                                            onPasswordChanged("Password successfully changed")
                                            onDismiss()
                                        } else {
                                            errorMessage = "Failed to change password: ${updateTask.exception?.message}"
                                        }
                                    } ?: run {
                                        isUpdating = false
                                        errorMessage = "User not found"
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !isVerifying && !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isVerifying || isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = UIBlack,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (currentStep == 1) "Verification" else "Change Password",
                        color = UIBlack,
                        style = AppFont.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            Button(
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
                colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentStep == 2) "Back" else "Cancel",
                    color = UIBlack,
                    style = AppFont.Medium
                )
            }
        }
    )
}

@Composable
fun DeleteAccountDialogGoogle(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UIWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFE53935).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Account?",
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
                    text = "Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.",
                    color = UIDarkGrey,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Delete", color = Color.White, style = AppFont.SemiBold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Cancel", color = UIBlack, style = AppFont.Medium)
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var deleteErrorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UIWhite,
        shape = RoundedCornerShape(24.dp),
        // ... (Icon bagian atas tetap sama)
        icon = {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFE53935).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Account?",
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
                // UPDATE: Menambahkan instruksi "confirm" di sini agar lebih jelas
                Text(
                    text = "This action cannot be undone. All your data will be permanently deleted. Please enter your password to confirm.",
                    color = UIDarkGrey,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                CustomTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = ""; deleteErrorMessage = "" },
                    // UPDATE: Mempersingkat placeholder agar tidak terpotong ikon mata
                    placeholder = "Enter password",
                    isPassword = true,
                    showPassword = showPassword,
                    onShowPasswordToggle = { showPassword = !showPassword }
                )

                if (deleteErrorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = deleteErrorMessage,
                        fontSize = 12.sp,
                        style = AppFont.Regular,
                        color = Color.Red
                    )
                }

                // Menampilkan error validasi field kosong (jika ada)
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
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
                    if (password.isEmpty()) {
                        errorMessage = "Password required"
                    } else {
                        onConfirm(password)
                        // onDismiss() // Sebaiknya jangan dismiss dulu sebelum sukses, tapi tergantung logic Anda
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp) // Tambahan padding agar tidak terlalu mepet
            ) {
                Text("Delete", color = Color.White, style = AppFont.SemiBold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Cancel", color = UIBlack, style = AppFont.Medium)
            }
        }
    )
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onShowPasswordToggle: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = UIDarkGrey) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onShowPasswordToggle) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide" else "Show",
                        tint = UIDarkGrey
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = UIGrey,
            unfocusedContainerColor = UIGrey,
            focusedIndicatorColor = UIAccentYellow,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

