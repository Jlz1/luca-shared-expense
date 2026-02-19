package com.example.luca.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.luca.R

// 1. Daftarkan filenya satu-satu sesuai nama di res/font
val InstrumentSansFamily = FontFamily(
    Font(R.font.is_regular, FontWeight.Normal),
    Font(R.font.is_medium, FontWeight.Medium),
    Font(R.font.is_semibold, FontWeight.SemiBold),
    Font(R.font.is_bold, FontWeight.Bold)
)

// 2. Shortcut buat dipanggil di UI
object AppFont {
    val Regular = TextStyle(
        fontFamily = InstrumentSansFamily,
        fontWeight = FontWeight.Normal
    )

    val Medium = TextStyle(
        fontFamily = InstrumentSansFamily,
        fontWeight = FontWeight.Medium
    )

    val SemiBold = TextStyle(
        fontFamily = InstrumentSansFamily,
        fontWeight = FontWeight.SemiBold
    )

    val Bold = TextStyle(
        fontFamily = InstrumentSansFamily,
        fontWeight = FontWeight.Bold
    )
}