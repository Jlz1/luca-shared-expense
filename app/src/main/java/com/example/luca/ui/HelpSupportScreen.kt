package com.example.luca.ui

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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import com.example.luca.ui.debounceBackClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit,
    onReportBugClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val debouncedBackClick = debounceBackClick(scope) { onBackClick() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help & Support",
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

            // ===== 1. HEADER & SEARCH =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(UIWhite, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "How can we help you?",
                    style = AppFont.Bold,
                    fontSize = 22.sp, // Agak besar biar welcoming
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Search for topics or questions",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar Mockup
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(UIBackground, RoundedCornerShape(25.dp)) // Pill shape
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = UIDarkGrey
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "e.g., split bill, forgot password",
                        style = AppFont.Regular,
                        fontSize = 14.sp,
                        color = UIDarkGrey.copy(alpha = 0.6f)
                    )
                }
            }

            // ===== 2. POPULAR TOPICS / FAQ =====
            SectionContainer(title = "Popular Topics") {
                FaqItem(question = "How to split a bill equally?")
                HorizontalDivider(color = UIGrey.copy(alpha = 0.5f))
                FaqItem(question = "Can I edit an activity after saving?")
                HorizontalDivider(color = UIGrey.copy(alpha = 0.5f))
                FaqItem(question = "How do I add friends?")
                HorizontalDivider(color = UIGrey.copy(alpha = 0.5f))
                FaqItem(question = "Payment methods supported")
            }

            // ===== 3. CONTACT & ACTIONS =====
            SectionContainer(title = "Still need help?") {
                // Email Support
                ContactActionItem(
                    icon = Icons.Default.Email,
                    title = "Email Support",
                    subtitle = "Get response within 24 hours",
                    onClick = { /* TODO: Open Email Intent */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Report a Bug (Navigasi ke screen ReportBug)
                ContactActionItem(
                    icon = Icons.Default.BugReport,
                    title = "Report a Bug",
                    subtitle = "Something not working?",
                    onClick = onReportBugClick
                )
            }

            // Footer Spacer
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// COMPONENT HELPERS
// ==========================================

@Composable
private fun SectionContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .background(UIWhite, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = AppFont.Bold,
            fontSize = 16.sp,
            color = UIBlack,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun FaqItem(question: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Expand FAQ */ }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = question,
            style = AppFont.Medium,
            fontSize = 14.sp,
            color = UIBlack,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ExpandMore, // Icon panah bawah
            contentDescription = "Expand",
            tint = UIDarkGrey
        )
    }
}

@Composable
private fun ContactActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp) // Padding luar container klik
            .clip(RoundedCornerShape(12.dp))
            .background(UIBackground.copy(alpha = 0.5f)) // Background agak gelap dikit
            .clickable(onClick = onClick)
            .padding(16.dp), // Padding dalam
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(UIWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UIAccentYellow, // Warna icon kuning biar pop
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack
            )
            Text(
                text = subtitle,
                style = AppFont.Regular,
                fontSize = 12.sp,
                color = UIDarkGrey
            )
        }

        // Arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = UIDarkGrey
        )
    }
}

@Preview
@Composable
fun HelpSupportPreview() {
    LucaTheme {
        HelpSupportScreen(onBackClick = {}, onReportBugClick = {})
    }
}