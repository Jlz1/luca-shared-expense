package com.example.luca.ui

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
import com.example.luca.ui.theme.*

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
            HelpCenterSection(title = "Selamat Datang") {
                Text(
                    text = "Kami di sini untuk membantu Anda mendapatkan hasil maksimal dari Luca. Jika Anda mengalami masalah atau memiliki pertanyaan, jangan ragu untuk menghubungi tim dukungan kami.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    lineHeight = 22.sp
                )
            }

            // ===== 2. COMMON ISSUES =====
            HelpCenterSection(title = "Masalah Umum") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Tidak Bisa Login",
                        description = "Pastikan Anda menggunakan email yang benar dan password Anda aman. Gunakan fitur 'Lupa Password' jika diperlukan."
                    )
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Masalah Sinkronisasi Data",
                        description = "Coba refresh aplikasi atau login kembali. Pastikan koneksi internet Anda stabil."
                    )
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Tidak Bisa Membuat Grup",
                        description = "Pastikan Anda memiliki koneksi internet yang baik dan sudah mengisi profil lengkap."
                    )
                    HelpCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Masalah Pembayaran",
                        description = "Hubungi tim dukungan kami untuk bantuan lebih lanjut mengenai masalah pembayaran atau transaksi."
                    )
                }
            }

            // ===== 3. QUICK TIPS =====
            HelpCenterSection(title = "Tips Cepat") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HelpCard(
                        icon = Icons.Default.Help,
                        title = "Kelola Grup Dengan Baik",
                        description = "Pastikan semua anggota grup memiliki peran yang jelas dan persetujuan untuk setiap pengeluaran bersama."
                    )
                    HelpCard(
                        icon = Icons.Default.Help,
                        title = "Gunakan Kategori",
                        description = "Gunakan kategori pengeluaran untuk melacak pengeluaran dengan lebih mudah dan membuat laporan lebih detail."
                    )
                    HelpCard(
                        icon = Icons.Default.Help,
                        title = "Periksa Laporan Reguler",
                        description = "Tinjau laporan pengeluaran secara teratur untuk memastikan akurasi dan mendeteksi masalah lebih awal."
                    )
                }
            }

            // ===== 4. CONTACT SUPPORT =====
            HelpCenterSection(title = "Hubungi Kami") {
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
                            text = "Butuh Bantuan?",
                            style = AppFont.Bold,
                            fontSize = 18.sp,
                            color = UIBlack,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Kirimkan email kepada tim dukungan kami untuk bantuan lebih lanjut.",
                            style = AppFont.Regular,
                            fontSize = 14.sp,
                            color = UIDarkGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }

                    // Email Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:lucasharedexpense@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Bantuan Luca - Shared Expense")
                            }
                            context.startActivity(Intent.createChooser(intent, "Kirim Email"))
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
                        Text(
                            text = "lucasharedexpense@gmail.com",
                            style = AppFont.SemiBold,
                            fontSize = 14.sp,
                            color = UIBlack
                        )
                    }

                    Text(
                        text = "Balas kami dalam 24 jam",
                        style = AppFont.Regular,
                        fontSize = 12.sp,
                        color = UIDarkGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ===== 5. ADDITIONAL INFO =====
            HelpCenterSection(title = "Informasi Tambahan") {
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
                            text = "Kami berkomitmen untuk memberikan dukungan terbaik kepada Anda. Respons cepat dan solusi efektif adalah prioritas kami.",
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
                            text = "Baca FAQ kami di Privacy & Security dan Help & Support untuk jawaban cepat atas pertanyaan umum.",
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
