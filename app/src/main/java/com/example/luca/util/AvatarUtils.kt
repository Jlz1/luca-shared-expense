package com.example.luca.util

import com.example.luca.R

object AvatarUtils {
    // Mendaftarkan 10 Avatar: Pair("NamaDatabase", R.drawable.NamaFile)
    val avatars = listOf(
        "avatar_1" to R.drawable.avatar_1,
        "avatar_2" to R.drawable.avatar_2,
        "avatar_3" to R.drawable.avatar_3,
        "avatar_4" to R.drawable.avatar_4,
        "avatar_5" to R.drawable.avatar_5,
        "avatar_6" to R.drawable.avatar_6,
        "avatar_7" to R.drawable.avatar_7,
        "avatar_8" to R.drawable.avatar_8,
        "avatar_9" to R.drawable.avatar_9,
        "avatar_10" to R.drawable.avatar_10
    )

    // Fungsi: Ubah String (misal "avatar_3") jadi Gambar (R.drawable.avatar_3)
    fun getAvatarResId(avatarName: String): Int {
        // Kalau nama tidak ditemukan atau null, default ke avatar_1
        return avatars.find { it.first == avatarName }?.second ?: R.drawable.avatar_1
    }
}