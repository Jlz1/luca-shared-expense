package com.luca.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luca.shared.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import com.luca.shared.ui.debounceBackClick

// ==========================================
// HELPER COMPOSABLES
// ==========================================

@Composable
fun PrivacySecuritySection(
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


// ==========================================
// MAIN SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val debouncedBackClick = debounceBackClick(scope) { onBackClick() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy & Security",
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

            // ===== 1. INTRO SECTION =====
            PrivacySecuritySection(title = "About Privacy & Security in Luca") {
                Text(
                    text = "Luca is a shared expense management app designed with a primary focus on user data security and privacy. We are committed to protecting your personal information with the highest security standards.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    lineHeight = 22.sp
                )
            }

            // ===== 2. DATA PROTECTION =====
            PrivacySecuritySection(title = "Data Protection") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "End-to-End Encryption",
                        description = "All your transactions and expense data are encrypted using the AES-256 algorithm. Only you and your group members can access the expense details."
                    )
                    PrivacyBulletPoint(
                        title = "Encrypted Database",
                        description = "Sensitive data is stored on encrypted servers with multi-layered security protocols and automatic backups to prevent data loss."
                    )
                    PrivacyBulletPoint(
                        title = "Secure Authentication",
                        description = "We use Firebase Authentication to ensure that only authorized users can access their accounts."
                    )
                }
            }

            // ===== 3. PRIVACY POLICY =====
            PrivacySecuritySection(title = "Privacy Policy") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "Minimal Data Usage",
                        description = "We only collect data necessary for the app's functionality. We do not share your personal data with third parties without your explicit consent."
                    )
                    PrivacyBulletPoint(
                        title = "Full User Control",
                        description = "You have full control over your data. You can delete your account at any time and all related data will be permanently removed from our servers."
                    )
                    PrivacyBulletPoint(
                        title = "Complete Transparency",
                        description = "We are transparent about how your data is used. There is no hidden data collection or unauthorized user tracking."
                    )
                }
            }

            // ===== 4. USER SECURITY =====
            PrivacySecuritySection(title = "User Security") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "Session Management",
                        description = "Your session will automatically end after 15 minutes of inactivity to prevent unauthorized access if your device is left unattended."
                    )
                    PrivacyBulletPoint(
                        title = "Identity Verification",
                        description = "When performing sensitive activities such as password changes, we require verification via email to ensure your account's security."
                    )
                    PrivacyBulletPoint(
                        title = "Activity Monitoring",
                        description = "We monitor suspicious activity and will notify you if there is a login from an unrecognized device or location."
                    )
                }
            }

            // ===== 5. COMPLIANCE =====
            PrivacySecuritySection(title = "Compliance & Standards") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "GDPR Compliant",
                        description = "Our app complies with the General Data Protection Regulation (GDPR) to ensure user data protection worldwide."
                    )
                    PrivacyBulletPoint(
                        title = "Security Certificates",
                        description = "Our infrastructure uses SSL/TLS certificates and follows the latest industry security best practices."
                    )
                    PrivacyBulletPoint(
                        title = "Regular Security Audits",
                        description = "We conduct regular security audits and penetration testing to identify and fix potential vulnerabilities."
                    )
                }
            }

            // ===== 6. CONTACT SUPPORT =====
            PrivacySecuritySection(title = "Privacy & Security Questions") {
                Text(
                    text = "If you have any questions or concerns about your data privacy and security, please contact our support team via the Help & Support menu in the app or send an email to support lucasharedexpense@gmail.com.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacyBulletPoint(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bullet point
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(UIAccentYellow, CircleShape)
                .padding(top = 6.dp)
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

@Preview
@Composable
fun PrivacySecurityPreview() {
    LucaTheme {
        PrivacySecurityScreen(onBackClick = {})
    }
}
