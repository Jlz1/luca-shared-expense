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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataSaverOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*

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

            // ===== 1. INTRO SECTION =====
            PrivacySecuritySection(title = "Tentang Privacy & Security Luca") {
                Text(
                    text = "Luca adalah aplikasi manajemen pengeluaran bersama yang dirancang dengan fokus utama pada keamanan dan privasi data pengguna. Kami berkomitmen untuk melindungi informasi pribadi Anda dengan standar keamanan tertinggi.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIDarkGrey,
                    lineHeight = 22.sp
                )
            }

            // ===== 2. DATA PROTECTION =====
            PrivacySecuritySection(title = "Perlindungan Data") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "Enkripsi End-to-End",
                        description = "Semua transaksi dan data pengeluaran Anda dienkripsi menggunakan algoritma AES-256. Hanya Anda dan anggota grup yang dapat mengakses detail pengeluaran."
                    )
                    PrivacyBulletPoint(
                        title = "Database Terenkripsi",
                        description = "Data sensitif disimpan di server terenkripsi dengan protokol keamanan berlapis dan backup otomatis untuk mencegah kehilangan data."
                    )
                    PrivacyBulletPoint(
                        title = "Otentikasi Aman",
                        description = "Kami menggunakan Firebase Authentication untuk memastikan hanya pengguna yang berwenang dapat mengakses akun mereka."
                    )
                }
            }

            // ===== 3. PRIVACY POLICY =====
            PrivacySecuritySection(title = "Kebijakan Privasi") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "Penggunaan Data Minimal",
                        description = "Kami hanya mengumpulkan data yang diperlukan untuk fungsi aplikasi. Kami tidak membagikan data pribadi Anda kepada pihak ketiga tanpa persetujuan eksplisit."
                    )
                    PrivacyBulletPoint(
                        title = "Kontrol Penuh Pengguna",
                        description = "Anda memiliki kendali penuh atas data Anda. Kapan saja Anda dapat menghapus akun dan semua data terkait akan dihapus secara permanen dari server kami."
                    )
                    PrivacyBulletPoint(
                        title = "Transparansi Lengkap",
                        description = "Kami transparan tentang bagaimana data Anda digunakan. Tidak ada pengumpulan data tersembunyi atau pelacakan pengguna yang tidak sah."
                    )
                }
            }

            // ===== 4. USER SECURITY =====
            PrivacySecuritySection(title = "Keamanan Pengguna") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "Manajemen Sesi",
                        description = "Sesi Anda akan otomatis berakhir setelah 15 menit tidak aktif untuk mencegah akses tidak sah jika perangkat Anda ditinggalkan."
                    )
                    PrivacyBulletPoint(
                        title = "Verifikasi Identitas",
                        description = "Ketika melakukan aktivitas sensitif seperti perubahan password, kami meminta verifikasi melalui email untuk memastikan keamanan akun Anda."
                    )
                    PrivacyBulletPoint(
                        title = "Monitoring Aktivitas",
                        description = "Kami memantau aktivitas mencurigakan dan akan memberitahu Anda jika ada login dari perangkat atau lokasi yang tidak dikenali."
                    )
                }
            }

            // ===== 5. COMPLIANCE =====
            PrivacySecuritySection(title = "Kepatuhan & Standar") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyBulletPoint(
                        title = "GDPR Compliant",
                        description = "Aplikasi kami mematuhi Regulasi Perlindungan Data Umum (GDPR) untuk memastikan perlindungan data pengguna di seluruh dunia."
                    )
                    PrivacyBulletPoint(
                        title = "Sertifikat Keamanan",
                        description = "Infrastructure kami menggunakan sertifikat SSL/TLS dan mengikuti best practices keamanan industri terkini."
                    )
                    PrivacyBulletPoint(
                        title = "Audit Keamanan Berkala",
                        description = "Kami melakukan audit keamanan berkala dan penetration testing untuk mengidentifikasi dan memperbaiki potensi kerentanan."
                    )
                }
            }

            // ===== 6. CONTACT SUPPORT =====
            PrivacySecuritySection(title = "Pertanyaan Privasi & Keamanan") {
                Text(
                    text = "Jika Anda memiliki pertanyaan atau kekhawatiran tentang privasi dan keamanan data Anda, silakan hubungi tim dukungan kami melalui menu Help & Support di aplikasi atau kirimkan email ke support lucasharedexpense@gmail.com.",
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
