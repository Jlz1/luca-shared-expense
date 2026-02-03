package com.example.luca.model

/**
 * Model untuk menyimpan hasil settlement dari SmartSplitBill algorithm.
 * Akan disimpan di Firebase sebagai bagian dari Event.
 */
data class Settlement(
    val id: String = "",
    val fromName: String = "",        // Nama orang yang hutang
    val fromAvatarName: String = "",  // Avatar orang yang hutang
    val toName: String = "",          // Nama orang yang dibayar
    val toAvatarName: String = "",    // Avatar orang yang dibayar
    val amount: Long = 0L,            // Jumlah yang harus dibayar
    val isPaid: Boolean = false       // Status pembayaran
)

/**
 * Data class untuk menyimpan seluruh hasil split bill calculation.
 * Ini akan disimpan sebagai field `settlementResult` di Event document.
 */
data class SettlementResult(
    val settlements: List<Settlement> = emptyList(),
    val totalExpense: Long = 0L,
    val calculatedAt: Long = System.currentTimeMillis()
)

