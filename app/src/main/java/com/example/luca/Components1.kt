package com.example.luca

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentRed
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun StackedAvatarRo(
    spacing: Int = -10,
    avatars: List<String>,
    maxVisible: Int = 4,
    itemSize: Dp = 40.dp // Tambahan: Ukuran standar untuk avatar & counter
) {
    // 1. Hitung Logic Sisa
    val isOverflow = avatars.size > maxVisible

    // Jika overflow, kurangi 1 slot untuk tempat counter (+N)
    val visibleCount = if (isOverflow) maxVisible - 1 else avatars.size
    val remainingCount = avatars.size - visibleCount

    Row(
        horizontalArrangement = Arrangement.spacedBy((spacing).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2. Render Foto (Looping sebanyak visibleCount)
        for (i in 0 until visibleCount) {
            // Kita bungkus AvatarItem biar bisa dikontrol zIndex-nya dari sini jika perlu
            // Atau asumsikan AvatarItem sudah handle size
            Box(
                modifier = Modifier
                    .zIndex((visibleCount - i).toFloat()) // Biar yang kiri selalu di atas (tumpukan menurun ke kanan)
            ) {
                AvatarIte(
                    imageCode = avatars[i],
                    size = itemSize,
                    zIndex = (visibleCount - i).toFloat()
                )
            }
        }

        // 3. Render Counter Overflow (Jika ada sisa)
        if (isOverflow) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .zIndex(0f) // Paling bawah tumpukannya
                    .size(itemSize)
                    .clip(CircleShape)
                    .background(UIDarkGrey)
                    // Optional: Kasih border putih biar misah sama foto sebelumnya
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = "+$remainingCount",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Komponen Dummy untuk AvatarItem (Hanya sebagai contoh agar kode di atas tidak error)
@Composable
fun AvatarIte(imageCode: String, zIndex: Float, size: Dp = 40.dp) {
    val commonModifier = Modifier
        .size(size)
        .zIndex(zIndex)
        .clip(CircleShape)
        .border(2.dp, UIWhite, CircleShape)

    if (imageCode == "debug") {
        Box(modifier = commonModifier.background(Color.Gray))
    } else {
        // Ambil ID resource berdasarkan kode string/angka
        val imageRes = getResourceId(imageCode)

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = commonModifier.background(UIWhite)
        )
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun getResourceId(name: String): Int {
    val context = androidx.compose.ui.platform.LocalContext.current
    return context.resources.getIdentifier(
        "avatar_$name", // Nama file tanpa ekstensi
        "drawable",
        context.packageName
    )
}

@Preview(showBackground = true)
@Composable
fun StackedAvatarRoPreview() {
    LucaTheme {
        Box(modifier = Modifier.padding(20.dp).background(UIAccentYellow)) {
            StackedAvatarRo(
                avatars = listOf("debug", "debug", "debug", "debug", "debug"),
                maxVisible = 4
            )
        }
    }
}
