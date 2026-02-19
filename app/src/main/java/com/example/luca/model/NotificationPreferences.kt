package com.example.luca.model

data class NotificationPreferences(
    // Push Notifications
    var pushEnabled: Boolean = true,
    var pushNewExpense: Boolean = true,
    var pushPaymentReminder: Boolean = true,
    var pushGroupInvite: Boolean = true,
    var pushExpenseUpdate: Boolean = true,

    // Email Notifications
    var emailEnabled: Boolean = true,
    var emailWeeklySummary: Boolean = true,
    var emailPaymentReminder: Boolean = true,
    var emailGroupActivity: Boolean = false,

    // Do Not Disturb
    var doNotDisturbEnabled: Boolean = false,
    var doNotDisturbStart: String = "22:00",
    var doNotDisturbEnd: String = "07:00"
)
