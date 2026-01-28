package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.R
import com.example.luca.ui.theme.*
import androidx.compose.material3.ExperimentalMaterial3Api

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
            // ===== HEADER / IDENTITAS APLIKASI =====
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

            // ===== DESKRIPSI APLIKASI =====
            SectionContainer(title = "Tentang Luca") {
                Text(
                    text = "Luca adalah aplikasi mobile yang dirancang untuk memudahkan Anda dalam membagi biaya bersama teman, keluarga, atau kelompok. Dengan fitur yang intuitif dan user-friendly, Luca membantu mengelola pengeluaran bersama dan melacak siapa yang berhutang kepada siapa.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Luca dirancang untuk menghilangkan kebingungan dalam menghitung pembagian biaya dan memastikan semua orang mendapatkan perhitungan yang adil dan transparan.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
            }

            // ===== VISI =====
            SectionContainer(title = "Visi Kami") {
                Text(
                    text = "Menjadi aplikasi pilihan utama dalam memudahkan pembagian biaya dengan cara yang fair, transparan, dan menyenangkan bagi semua pengguna.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
            }

            // ===== MISI =====
            SectionContainer(title = "Misi Kami") {
                MissionItem(number = "1", text = "Menyediakan platform yang mudah digunakan untuk mengelola pengeluaran bersama")
                MissionItem(number = "2", text = "Memastikan perhitungan yang akurat dan transparan dalam pembagian biaya")
                MissionItem(number = "3", text = "Mengurangi konflik dan kebingungan terkait pembagian pengeluaran")
                MissionItem(number = "4", text = "Terus berinovasi untuk memberikan fitur terbaik kepada pengguna")
            }

            // ===== NILAI DAN PRINSIP =====
            SectionContainer(title = "Nilai Kami") {
                ValueItem(
                    title = "Transparansi",
                    description = "Semua transaksi dan perhitungan ditampilkan dengan jelas dan jujur"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueItem(
                    title = "Kepercayaan",
                    description = "Data pengguna dijaga dengan aman dan tidak dibagikan tanpa izin"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueItem(
                    title = "Kemudahan",
                    description = "Antarmuka yang intuitif sehingga siapa saja bisa menggunakannya dengan mudah"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ValueItem(
                    title = "Inovasi",
                    description = "Terus mengembangkan fitur baru berdasarkan masukan pengguna"
                )
            }

            // ===== TIM PENGEMBANG =====
            SectionContainer(title = "Tim Pengembang") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    repeat(3) { rowIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(2) { colIndex ->
                                val index = rowIndex * 2 + colIndex
                                if (index < 5) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        TeamMemberCard(
                                            name = when (index) {
                                                0 -> "Beben Rafli Luhut Tua Sianipar"
                                                1 -> "Jeremy Emmanuel Susilo"
                                                2 -> "Made Abel Surya Mahotama"
                                                3 -> "Michael Kevin Pratama"
                                                else -> "Steven Kukilo Seto"
                                            },
                                            role = when (index) {
                                                0 -> "Full Stack Developer, Scrum Master"
                                                1 -> "Full Stack Developer, DevOps Engineer"
                                                2 -> "Full Stack Developer, Backend Developer"
                                                3 -> "Full Stack Developer, UI/UX Designer"
                                                else -> "Full Stack Developer, Product Owner"
                                            }
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // ===== TEKNOLOGI =====
            SectionContainer(title = "Teknologi yang Digunakan") {
                TechItem(tech = "Kotlin", description = "Bahasa pemrograman untuk Android")
                TechItem(tech = "Jetpack Compose", description = "Framework UI modern untuk Android")
                TechItem(tech = "Firebase", description = "Backend dan database real-time")
                TechItem(tech = "Material Design 3", description = "Design system modern")
            }

            // ===== KOMITMEN PENGGUNA =====
            SectionContainer(title = "Komitmen Kami kepada Anda") {
                Text(
                    text = "Kami berkomitmen untuk terus memberikan pengalaman terbaik kepada setiap pengguna Luca. Kami selalu terbuka terhadap masukan, saran, dan kritik constructive untuk terus meningkatkan kualitas aplikasi.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Pengembangan Luca adalah proses berkelanjutan yang melibatkan feedback dari komunitas pengguna. Terima kasih telah menjadi bagian dari perjalanan Luca!",
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

@Composable
private fun TeamMemberCard(name: String, role: String) {
    Column(
        modifier = Modifier
            .background(UIWhite, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(UIGrey, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = name,
                tint = UIDarkGrey,
                modifier = Modifier.size(40.dp)
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
                    imageVector = Icons.Default.Language,
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
