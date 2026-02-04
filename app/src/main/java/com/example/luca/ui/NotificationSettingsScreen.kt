package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.data.LucaFirebaseRepository
import com.example.luca.model.NotificationPreferences
import com.example.luca.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit
) {
    var preferences by remember { mutableStateOf(NotificationPreferences()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // Load notification preferences dari Firebase
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val repository = LucaFirebaseRepository()
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val loadedPrefs = withContext(Dispatchers.IO) {
                        repository.getNotificationPreferences(userId)
                    }
                    if (loadedPrefs != null) {
                        preferences = loadedPrefs
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationSettings", "Failed to load preferences: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Auto-hide success message
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(3000)
            showSuccessMessage = false
        }
    }

    // Function to save preferences
    fun savePreferences() {
        scope.launch {
            isSaving = true
            try {
                val repository = LucaFirebaseRepository()
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    withContext(Dispatchers.IO) {
                        repository.saveNotificationPreferences(userId, preferences)
                    }
                    successMessage = "Notification settings saved successfully"
                    showSuccessMessage = true
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationSettings", "Failed to save preferences: ${e.message}")
                successMessage = "Failed to save settings: ${e.message}"
                showSuccessMessage = true
            } finally {
                isSaving = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Notification Settings",
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

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = UIAccentYellow)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                ) {
                    // Header Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .background(UIWhite, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = UIAccentYellow,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Manage your notification preferences",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIDarkGrey,
                            textAlign = TextAlign.Center
                        )
                    }

                    // ===== PUSH NOTIFICATIONS SECTION =====
                    NotificationSection(
                        title = "Push Notifications",
                        icon = Icons.Default.PhoneAndroid
                    ) {
                        NotificationToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "Enable Push Notifications",
                            subtitle = "Receive notifications on this device",
                            checked = preferences.pushEnabled,
                            onCheckedChange = {
                                preferences = preferences.copy(pushEnabled = it)
                                savePreferences()
                            }
                        )

                        if (preferences.pushEnabled) {
                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            NotificationToggleItem(
                                icon = Icons.Default.Receipt,
                                title = "New Expenses",
                                subtitle = "When someone adds a new expense",
                                checked = preferences.pushNewExpense,
                                onCheckedChange = {
                                    preferences = preferences.copy(pushNewExpense = it)
                                    savePreferences()
                                }
                            )

                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            NotificationToggleItem(
                                icon = Icons.Default.AttachMoney,
                                title = "Payment Reminders",
                                subtitle = "Reminders for pending payments",
                                checked = preferences.pushPaymentReminder,
                                onCheckedChange = {
                                    preferences = preferences.copy(pushPaymentReminder = it)
                                    savePreferences()
                                }
                            )

                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            NotificationToggleItem(
                                icon = Icons.Default.People,
                                title = "Group Invitations",
                                subtitle = "When you're invited to a group",
                                checked = preferences.pushGroupInvite,
                                onCheckedChange = {
                                    preferences = preferences.copy(pushGroupInvite = it)
                                    savePreferences()
                                }
                            )

                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            NotificationToggleItem(
                                icon = Icons.Default.Receipt,
                                title = "Expense Updates",
                                subtitle = "When expenses are edited or deleted",
                                checked = preferences.pushExpenseUpdate,
                                onCheckedChange = {
                                    preferences = preferences.copy(pushExpenseUpdate = it)
                                    savePreferences()
                                }
                            )
                        }
                    }

                    // ===== EMAIL NOTIFICATIONS SECTION =====
                    NotificationSection(
                        title = "Email Notifications",
                        icon = Icons.Default.Email
                    ) {
                        NotificationToggleItem(
                            icon = Icons.Default.Email,
                            title = "Enable Email Notifications",
                            subtitle = "Receive notifications via email",
                            checked = preferences.emailEnabled,
                            onCheckedChange = {
                                preferences = preferences.copy(emailEnabled = it)
                                savePreferences()
                            }
                        )

                        if (preferences.emailEnabled) {
                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            NotificationToggleItem(
                                icon = Icons.Default.Receipt,
                                title = "Weekly Summary",
                                subtitle = "Get weekly expense summaries",
                                checked = preferences.emailWeeklySummary,
                                onCheckedChange = {
                                    preferences = preferences.copy(emailWeeklySummary = it)
                                    savePreferences()
                                }
                            )

                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            NotificationToggleItem(
                                icon = Icons.Default.AttachMoney,
                                title = "Payment Reminders",
                                subtitle = "Email reminders for payments",
                                checked = preferences.emailPaymentReminder,
                                onCheckedChange = {
                                    preferences = preferences.copy(emailPaymentReminder = it)
                                    savePreferences()
                                }
                            )

                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            NotificationToggleItem(
                                icon = Icons.Default.People,
                                title = "Group Activity",
                                subtitle = "Updates from your groups",
                                checked = preferences.emailGroupActivity,
                                onCheckedChange = {
                                    preferences = preferences.copy(emailGroupActivity = it)
                                    savePreferences()
                                }
                            )
                        }
                    }

                    // ===== DO NOT DISTURB SECTION =====
                    NotificationSection(
                        title = "Do Not Disturb",
                        icon = Icons.Default.Notifications
                    ) {
                        NotificationToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "Enable Do Not Disturb",
                            subtitle = "Mute notifications during specified hours",
                            checked = preferences.doNotDisturbEnabled,
                            onCheckedChange = {
                                preferences = preferences.copy(doNotDisturbEnabled = it)
                                savePreferences()
                            }
                        )

                        if (preferences.doNotDisturbEnabled) {
                            Divider(color = UIGrey.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    text = "Active Hours",
                                    style = AppFont.SemiBold,
                                    fontSize = 14.sp,
                                    color = UIBlack
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "From ${preferences.doNotDisturbStart} to ${preferences.doNotDisturbEnd}",
                                    style = AppFont.Regular,
                                    fontSize = 12.sp,
                                    color = UIDarkGrey
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Notifications will be muted during these hours",
                                    style = AppFont.Regular,
                                    fontSize = 12.sp,
                                    color = UIDarkGrey.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Save button (optional, since we auto-save)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Info text
                    Text(
                        text = "Changes are saved automatically",
                        style = AppFont.Regular,
                        fontSize = 12.sp,
                        color = UIDarkGrey,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
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
                            if (successMessage.contains("success", ignoreCase = true))
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            else
                                MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        successMessage,
                        color = UIWhite,
                        style = AppFont.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Saving indicator
            if (isSaving) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(UIBlack.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = UIWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Saving...",
                            color = UIWhite,
                            style = AppFont.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .background(
                color = UIWhite,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UIAccentYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = UIBlack
            )
        }
        content()
    }
}

@Composable
fun NotificationToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UIDarkGrey,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AppFont.Regular,
                fontSize = 12.sp,
                color = UIDarkGrey.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = UIWhite,
                checkedTrackColor = UIAccentYellow,
                uncheckedThumbColor = UIWhite,
                uncheckedTrackColor = UIGrey
            )
        )
    }
}

@Preview
@Composable
fun NotificationSettingsPreview() {
    LucaTheme {
        NotificationSettingsScreen(
            onBackClick = {}
        )
    }
}
