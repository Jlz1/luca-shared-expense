package com.example.luca.model // <-- Pastikan package-nya bener

data class User(
    val uid: String = "",           // ID unik dari Firebase Auth
    val email: String = "",         // Email user
    val username: String = "",      // Nama tampilan
    val photoUrl: String = "",      // Foto profil (opsional, buat nanti)
    val createdAt: Long = 0         // Tanggal dibuat (biar bisa diurutin)
)
// PENTING: Semua harus punya nilai default (= "")
// Supaya Firebase bisa otomatis mengubah data dari database menjadi object User ini.