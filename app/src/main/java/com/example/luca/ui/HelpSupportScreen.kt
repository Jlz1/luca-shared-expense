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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit,
    onReportBugClick: () -> Unit,
    onEmailSupportClick: () -> Unit
) {
    // State untuk mengontrol FAQ mana yang sedang expanded
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

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

            // ===== 1. HEADER & HELP CATEGORIES =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "How can we help you?",
                    style = AppFont.Bold,
                    fontSize = 22.sp,
                    color = UIBlack,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Help Categories
                HelpCategoryCard(
                    emoji = "💸",
                    title = "Transaction & Split Bill",
                    articles = listOf(
                        "How to create a new activity",
                        "Split bill equally among participants",
                        "Use custom split for unequal distribution",
                        "Edit or delete an activity",
                        "Understand payment summary and settlements"
                    ),
                    expandedIndex = expandedFaqIndex,
                    categoryIndex = -1,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == -1) null else -1 }
                )

                Spacer(modifier = Modifier.height(12.dp))

                HelpCategoryCard(
                    emoji = "👥",
                    title = "Friends & Groups",
                    articles = listOf(
                        "Add participants to your event",
                        "Create and manage contacts",
                        "Choose avatars for friends",
                        "Add or remove participants from activity",
                        "View transaction history per person"
                    ),
                    expandedIndex = expandedFaqIndex,
                    categoryIndex = -2,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == -2) null else -2 }
                )

                Spacer(modifier = Modifier.height(12.dp))

                HelpCategoryCard(
                    emoji = "⚙️",
                    title = "Account & Security",
                    articles = listOf(
                        "Change your profile and avatar",
                        "Manage created events",
                        "Backup and restore your data",
                        "Delete events or activities",
                        "Data security tips"
                    ),
                    expandedIndex = expandedFaqIndex,
                    categoryIndex = -3,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == -3) null else -3 }
                )
            }

            // ===== 2. POPULAR TOPICS / FAQ =====
            SectionContainer(title = "Popular Topics") {
                FaqItem(
                    question = "How to split a bill equally?",
                    answer = "To split a bill equally:\n\n1. Create a new activity and enter the bill details\n2. Add all participants who will share the cost\n3. Select 'Equal Split' option\n4. The total amount will be automatically divided equally among all participants\n5. Each person will see their share amount\n6. Save the activity to record the split",
                    isExpanded = expandedFaqIndex == 0,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 0) null else 0 }
                )
                HorizontalDivider(color = UIGrey.copy(alpha = 0.5f))
                FaqItem(
                    question = "Can I edit an activity after saving?",
                    answer = "Yes! You can edit any activity:\n\n1. Go to your event's activity list\n2. Tap on the activity you want to edit\n3. Click the edit icon (pencil) in the top right\n4. Make your changes to amount, participants, or split method\n5. Save changes\n\nNote: Editing an activity will update the settlement calculations automatically.",
                    isExpanded = expandedFaqIndex == 1,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 1) null else 1 }
                )
                HorizontalDivider(color = UIGrey.copy(alpha = 0.5f))
                FaqItem(
                    question = "How do I add friends?",
                    answer = "Adding friends to your event:\n\n1. Create or open an event\n2. Tap the '+' button in the participants section\n3. Choose to add from your contacts or create a new contact\n4. Enter their name and select an avatar\n5. Friends added will appear in all your activities for that event\n\nYou can add participants when creating activities too!",
                    isExpanded = expandedFaqIndex == 2,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 2) null else 2 }
                )
                HorizontalDivider(color = UIGrey.copy(alpha = 0.5f))
                FaqItem(
                    question = "How to add event?",
                    answer = "Creating a new event is easy:\n\n1. Tap the '+' button on the home screen\n2. Enter event title (e.g., 'Bali Trip 2024')\n3. Set event location (optional)\n4. Choose the event date\n5. Add participants who will join this event\n6. Tap 'Create Event'\n\nYou can then add activities and expenses to track shared costs for this event.",
                    isExpanded = expandedFaqIndex == 3,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 3) null else 3 }
                )
            }

            // ===== 3. CONTACT & ACTIONS =====
            SectionContainer(title = "Still need help?") {
                // Email Support
                ContactActionItem(
                    icon = Icons.Default.Email,
                    title = "Email Support",
                    subtitle = "Get response within 24 hours",
                    onClick = onEmailSupportClick
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
private fun HelpCategoryCard(
    emoji: String,
    title: String,
    articles: List<String>,
    expandedIndex: Int?,
    categoryIndex: Int,
    onToggle: () -> Unit
) {
    val isExpanded = expandedIndex == categoryIndex

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UIWhite, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = title,
                    style = AppFont.SemiBold,
                    fontSize = 16.sp,
                    color = UIBlack
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = UIDarkGrey,
                modifier = Modifier.size(24.dp)
            )
        }

        // Article list - hanya tampil jika expanded
        if (isExpanded) {
            Spacer(modifier = Modifier.height(16.dp))
            articles.forEach { article ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = AppFont.Regular,
                        fontSize = 14.sp,
                        color = UIAccentYellow,
                        modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                    )
                    Text(
                        text = article,
                        style = AppFont.Regular,
                        fontSize = 14.sp,
                        color = UIDarkGrey,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

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
private fun FaqItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = UIDarkGrey,
                modifier = Modifier.size(24.dp)
            )
        }

        // Answer section - hanya tampil jika expanded
        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = answer,
                style = AppFont.Regular,
                fontSize = 13.sp,
                color = UIDarkGrey,
                lineHeight = 20.sp,
                modifier = Modifier.padding(end = 24.dp)
            )
        }
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
        HelpSupportScreen(
            onBackClick = {},
            onReportBugClick = {},
            onEmailSupportClick = {}
        )
    }
}