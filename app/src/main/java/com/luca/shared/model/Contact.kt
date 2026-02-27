package com.luca.shared.model

data class BankAccountData(
    val bankName: String = "",
    val accountNumber: String = "",
    val bankLogo: String = ""
)

data class Contact(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val avatarName: String = "",
    val bankAccounts: List<BankAccountData> = emptyList()
)