package com.example.luca.util

object ValidationUtils {

    // Email regex pattern
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    // Password: min 8 chars, uppercase, lowercase, digit, special char ($,@,#,!,.)
    private val PASSWORD_REGEX = Regex(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\$@#!.])[A-Za-z\\d\$@#!.]{8,}$"
    )

    // Dangerous characters for sanitization (prevent script injection)
    private val DANGEROUS_CHARS = Regex("[<>\"';&|`\\\\]")

    /**
     * Check if email format is valid
     */
    fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    /**
     * Check if password meets all requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character ($,@,#,!,.)
     */
    fun isPasswordValid(password: String): Boolean {
        return password.isNotBlank() && PASSWORD_REGEX.matches(password)
    }

    /**
     * Sanitize input by removing potentially dangerous characters
     */
    fun sanitizeInput(input: String): String {
        return input.trim().replace(DANGEROUS_CHARS, "")
    }

    /**
     * Get specific error message for email validation
     */
    fun getEmailError(email: String): String? {
        return when {
            email.isBlank() -> "Email tidak boleh kosong"
            !EMAIL_REGEX.matches(email.trim()) -> "Format email tidak valid (contoh: user@email.com)"
            else -> null
        }
    }

    /**
     * Get specific error message for password validation
     * Returns detailed message about what's missing
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isBlank() -> "Password tidak boleh kosong"
            password.length < 8 -> "Password minimal 8 karakter"
            !password.any { it.isUpperCase() } -> "Password harus mengandung huruf besar"
            !password.any { it.isLowerCase() } -> "Password harus mengandung huruf kecil"
            !password.any { it.isDigit() } -> "Password harus mengandung angka"
            !password.any { it in "\$@#!." } -> "Password harus mengandung simbol (\$,@,#,!,.)"
            else -> null
        }
    }

    /**
     * Check if both email and password are valid for form submission
     */
    fun isLoginFormValid(email: String, password: String): Boolean {
        return isEmailValid(email) && isPasswordValid(password)
    }
}

