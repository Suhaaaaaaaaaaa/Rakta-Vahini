package com.example.raktavahini.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isStrongEnoughPassword(password: String): Boolean {
        return password.length >= 6
    }
}
