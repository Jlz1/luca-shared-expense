package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun UserProfileOverlay() {
    // State untuk text field
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    // Card Utama (Container Putih)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Margin luar
        // .height(IntrinsicSize.Min) // Opsional jika ingin fit content
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(all = 20.dp) // Padding dalam card
        ) {

            // --- BAGIAN ATAS (Header: Icon X, Foto, Icon Centang) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Tombol Silang (Kiri)
                IconButton(
                    onClick = { /* TODO: Nanti ditaro sini logic buat Close/Cancel */ },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(32.dp),
                        tint = UIBlack
                    )
                }

                // Foto Placeholder (Tengah)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(135.dp)
                        .clip(CircleShape)
                        .background(UIGrey)
                        .align(Alignment.Center)
                        .clickable { /* TODO: Nanti ditaro sini logic buat ambil foto */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Upload Photo",
                        tint = UIDarkGrey,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Tombol Centang (Kanan)
                IconButton(
                    onClick = { /* TODO: Nanti ditaro sini logic buat Save/Submit */ },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BAGIAN TENGAH (Input Fields) ---

            // Input Name
            CustomRoundedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Name",
                backgroundColor = UIGrey
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Phone
            CustomRoundedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "Phone Number (Optional)",
                backgroundColor = UIGrey
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- BAGIAN BAWAH (Bank Accounts + Plus Button) ---

            Text(
                text = "Bank Accounts",
                fontSize = 18.sp,
                style = AppFont.Bold,
                color = UIBlack
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tombol Plus (+)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(UIAccentYellow) // Warna kuning/oranye
                    .clickable { /* TODO: Nanti ditaro sini logic buat nambah akun bank */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Bank Account",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Komponen Helper supaya Text Field nya rounded banget dan clean (tanpa garis bawah)
@Composable
fun CustomRoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    backgroundColor: Color
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp), // Padding kiri kanan biar ga nempel pinggir
        shape = RoundedCornerShape(50), // Bikin rounded banget
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah saat aktif
            unfocusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah saat mati
        ),
        singleLine = true
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
fun PreviewOverlay() {
    // Ceritanya background gelap biar keliatan kaya overlay
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        UserProfileOverlay()
    }
}