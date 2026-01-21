package com.example.luca.model

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",

    // UBAH DARI photoUrl KE avatarName
    // Default-nya kasih "avatar_1" biar gak error kalau datanya kosong
    val avatarName: String = "avatar_1",

    val createdAt: Long = 0
)