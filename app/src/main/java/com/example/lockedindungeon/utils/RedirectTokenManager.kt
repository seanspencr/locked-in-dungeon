package com.example.lockedindungeon.utils

import java.util.UUID

object RedirectTokenManager {
    private var token: String? = null

    fun generateToken(): String {
        val t = UUID.randomUUID().toString()
        token = t
        return t
    }

    fun validateToken(receivedToken: String?): Boolean {
        val isValid = receivedToken != null && receivedToken == token
        if (isValid) {
            token = null
        }
        return isValid
    }

    fun clearToken() {
        token = null
    }
}