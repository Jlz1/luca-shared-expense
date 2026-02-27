package com.luca.shared.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luca.shared.ui.theme.*

// ==========================================
// HELPER COMPOSABLES
// ==========================================

@Composable
fun HelpCenterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = AppFont.Bold,
            fontSize = 18.sp,
            color = UIAccentYellow,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun HelpCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UIAccentYellow,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = AppFont.SemiBold,
                    fontSize = 14.sp,
                    color = UIBlack
                )
                Text(
                    text = description,
                    style = AppFont.Regular,
                    fontSize = 13.sp,
                    color = UIDarkGrey,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ==========================================
// MAIN SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help Center",
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

            // ===== 1. WELCOME SECTION =====
            HelpCenterSection(title = "Welcome") {
                Text(
                    text = "We are here to help you get the most out of Luca. If you encounter issues or have questions, feel free to contact our support team.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    lineHeight = 22.sp
                )
            }

            // ===== 2. COMMON ISSUES =====
            HelpCenterSection(title = "Common Issues") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Cannot Login",
                        description = "Ensure you use the correct email and your password is secure. Use the 'Forgot Password' feature if needed."
                    )
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Data Sync Issues",
                        description = "Try refreshing the app or logging in again. Ensure your internet connection is stable."
                    )
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Cannot Create Group",
                        description = "Ensure you have a good internet connection and have completed your profile details."
                    )
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Payment Issues",
                        description = "Contact our support team for further assistance regarding payment or transaction issues."
                    )
                }
            }

            // ===== 3. QUICK TIPS =====
            HelpCenterSection(title = "Quick Tips") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HelpCard(
                        icon = Icons.Default.Help,
                        title = "Manage Groups Effectively",
                        description = "Ensure all group members have clear roles and approval for every shared expense."
                    )
                    HelpCard(
                        icon = Icons.Default.Help,
                        title = "Use Categories",
                        description = "Use expense categories to track spending easily and create more detailed reports."
                    )
                    HelpCard(
                        icon = Icons.Default.Help,
                        title = "Check Reports Regularly",
                        description = "Review expense reports regularly to ensure accuracy and detect issues early."
                    )
                }
            }

            // ===== 4. CONTACT SUPPORT =====
            HelpCenterSection(title = "Contact Us") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(UIWhite, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = UIAccentYellow,
                        modifier = Modifier.size(48.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Need Help?",
                            style = AppFont.Bold,
                            fontSize = 18.sp,
                            color = UIBlack,
                            textAlign = TextAlign.Center
                        )
                        // Perbaikan Layout: Email ditampilkan di sini agar bisa wrap text jika font besar
                        Text(
                            text = "Email us at lucasharedexpense@gmail.com for further assistance.",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIDarkGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }

                    // Email Button - Diubah teksnya agar aman untuk semua ukuran font
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:lucasharedexpense@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Luca Support - Shared Expense")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UIAccentYellow
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = UIBlack,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        // Teks tombol dipersingkat agar tidak terpotong
                        Text(
                            text = "Send Email",
                            style = AppFont.SemiBold,
                            fontSize = 14.sp,
                            color = UIBlack
                        )
                    }

                    Text(
                        text = "We typically reply within 24 hours",
                        style = AppFont.Regular,
                        fontSize = 12.sp,
                        color = UIDarkGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ===== 5. ADDITIONAL INFO =====
            HelpCenterSection(title = "Additional Information") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(UIWhite, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = UIAccentYellow,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = "We are committed to providing you with the best support. Fast response and effective solutions are our priority.",
                            style = AppFont.Regular,
                            fontSize = 13.sp,
                            color = UIDarkGrey,
                            lineHeight = 20.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(UIWhite, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = null,
                            tint = UIAccentYellow,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = "Read our FAQ in Privacy & Security and Help & Support for quick answers to common questions.",
                            style = AppFont.Regular,
                            fontSize = 13.sp,
                            color = UIDarkGrey,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
fun HelpCenterPreview() {
    LucaTheme {
        HelpCenterScreen(onBackClick = {})
    }
}