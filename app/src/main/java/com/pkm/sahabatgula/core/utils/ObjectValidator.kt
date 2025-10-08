package com.pkm.sahabatgula.core.utils

object Validator {

    fun validateUsername(username: String): String? {
        val hasMinLength = username.length >= 3
        val hasNoSpaces = !username.contains(" ")

        return when {
            username.isBlank() -> "Username tidak boleh kosong"
            !hasMinLength || !hasNoSpaces -> "Username minimal 3 karakter dan tidak boleh mengandung spasi"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email tidak boleh kosong"
            !email.contains("@") -> "Email harus mengandung '@'"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        if (password.isBlank()) return "Password tidak boleh kosong"
        return if (hasMinLength && hasUpperCase && hasLowerCase && hasDigit ) {
            null
        } else {
            "Gunakan minimal 8 karakter, dengan kombinasi huruf besar, huruf kecil, angka"
        }
    }
}
