package com.example.lockedindungeon.utils

import java.security.MessageDigest

fun hash(str : String) : String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(str.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

fun isEqualHashed(query : String, hashed : String) : Boolean{
    return hash(query) == hashed
}
