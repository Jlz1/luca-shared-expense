package com.example.luca.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Language // Digunakan sebagai icon LinkedIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onBackClick: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About Us",
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
            // ===== HEADER / APP IDENTITY =====
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
                Box(modifier = Modifier.size(80.dp).clip(CircleShape)) {
                    // Pastikan ic_luca_logo ada di drawable
                    Icon(
                        painter = painterResource(R.drawable.ic_luca_logo),
                        contentDescription = "Luca Logo",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Luca",
                    style = AppFont.Bold,
                    fontSize = 32.sp,
                    color = UIBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Split Expense Made Simple",
                    style = AppFont.Medium,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0",
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey
                )
            }

            // ===== ABOUT LUCA =====
            SectionContainer(title = "About Luca") {
                Text(
                    text = "Luca is a mobile application designed to make splitting expenses with friends, family, or groups easier. With intuitive and user-friendly features, Luca helps manage shared expenses and track who owes whom.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Luca is designed to eliminate confusion in calculating cost splits and ensure everyone gets a fair and transparent calculation.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
            }

            // ===== VISION =====
            SectionContainer(title = "Our Vision") {
                Text(
                    text = "To be the top choice application for facilitating expense splitting in a fair, transparent, and fun way for all users.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
            }

            // ===== MISSION =====
            SectionContainer(title = "Our Mission") {
                MissionItem(number = "1", text = "Provide an easy-to-use platform for managing shared expenses")
                MissionItem(number = "2", text = "Ensure accurate and transparent calculations in cost splitting")
                MissionItem(number = "3", text = "Reduce conflict and confusion regarding shared expenses")
                MissionItem(number = "4", text = "Continuously innovate to provide the best features for users")
            }

            // ===== VALUES =====
            SectionContainer(title = "Our Values") {
                ValueItem(
                    title = "Transparency",
                    description = "All transactions and calculations are displayed clearly and honestly"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueItem(
                    title = "Trust",
                    description = "User data is kept secure and not shared without permission"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueItem(
                    title = "Simplicity",
                    description = "Intuitive interface so anyone can use it easily"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueItem(
                    title = "Innovation",
                    description = "Continuously developing new features based on user feedback"
                )
            }

            // ===== DEVELOPMENT TEAM =====
            SectionContainer(title = "Development Team") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Beben & Jeremy
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TeamMemberCard(
                                name = "Beben Rafli Luhut Tua Sianipar",
                                role = "Full Stack Developer, Scrum Master",
                                imageRes = R.drawable.pic_beben
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TeamMemberCard(
                                name = "Jeremy Emmanuel Susilo",
                                role = "Full Stack Developer, DevOps Engineer",
                                imageRes = R.drawable.pic_jeremy
                            )
                        }
                    }

                    // Row 2: Abel & Kevin
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TeamMemberCard(
                                name = "Made Abel Surya Mahotama",
                                role = "Full Stack Developer, Quality Assurance",
                                imageRes = R.drawable.pic_abel
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TeamMemberCard(
                                name = "Michael Kevin Pratama",
                                role = "Full Stack Developer, UI/UX Designer",
                                imageRes = R.drawable.pic_kevin
                            )
                        }
                    }

                    // Row 3: Steven (Centered/Single)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Agar ukurannya konsisten dengan card lain, kita batasi width-nya 50% - padding
                        Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                            TeamMemberCard(
                                name = "Steven Kukilo Seto",
                                role = "Full Stack Developer, Product Owner",
                                imageRes = R.drawable.pic_steven
                            )
                        }
                    }
                }
            }

            // ===== TECHNOLOGIES =====
            SectionContainer(title = "Technologies Used") {
                TechItem(tech = "Kotlin", description = "Programming language for Android")
                TechItem(tech = "Jetpack Compose", description = "Modern UI toolkit for Android")
                TechItem(tech = "Firebase", description = "Backend and real-time database")
                TechItem(tech = "Material Design 3", description = "Modern design system")
            }

            // ===== COMMITMENT =====
            SectionContainer(title = "Our Commitment to You") {
                Text(
                    text = "We are committed to continuously providing the best experience for every Luca user. We are always open to input, suggestions, and constructive criticism to improve the app's quality.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Luca's development is an ongoing process involving feedback from the user community. Thank you for being part of Luca's journey!",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
            }

            // ===== COPYRIGHT =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(bottom = 24.dp))
                Text(
                    text = "© 2026 Luca. All rights reserved.",
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Made with ❤️ by Luca Team",
                    style = AppFont.Medium,
                    fontSize = 12.sp,
                    color = UIAccentYellow,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
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
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .background(
                color = UIWhite,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = AppFont.Bold,
            fontSize = 18.sp,
            color = UIBlack,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
private fun MissionItem(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(UIAccentYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = UIBlack
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = AppFont.Regular,
            fontSize = 14.sp,
            color = UIBlack,
            lineHeight = 21.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun ValueItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            style = AppFont.SemiBold,
            fontSize = 16.sp,
            color = UIBlack
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = AppFont.Regular,
            fontSize = 14.sp,
            color = UIDarkGrey,
            lineHeight = 20.sp
        )
    }
}

// Update TeamMemberCard untuk menerima image resource ID
@Composable
private fun TeamMemberCard(name: String, role: String, imageRes: Int) {
    Column(
        modifier = Modifier
            .background(UIWhite, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Photo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(UIGrey),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = name,
            style = AppFont.SemiBold,
            fontSize = 14.sp,
            color = UIBlack,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = role,
            style = AppFont.Regular,
            fontSize = 12.sp,
            color = UIDarkGrey,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Social Media Buttons (Placeholder Action)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "GitHub",
                    tint = UIBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = { },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language, // LinkedIn Icon Placeholder
                    contentDescription = "LinkedIn",
                    tint = UIBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = { },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Instagram",
                    tint = UIBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TechItem(tech: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(UIAccentYellow, CircleShape)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = tech,
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = AppFont.Regular,
                fontSize = 12.sp,
                color = UIDarkGrey
            )
        }
    }
}