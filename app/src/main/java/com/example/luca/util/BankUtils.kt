package com.example.luca.util

object BankUtils {
    val availableBanks = listOf(
        "BCA", "BRI", "BNI", "Mandiri", "Blu", "Others"
    )

    fun generateLogoFileName(bankName: String): String {
        return "bank_logo_${bankName.lowercase()}"
    }
}