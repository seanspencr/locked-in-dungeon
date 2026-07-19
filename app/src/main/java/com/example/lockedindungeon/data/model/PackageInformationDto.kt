package com.example.lockedindungeon.data.model

data class PackageInformationDto(
    val packageName : String,
    val displayName : String,
    var isBlocked : Boolean = false
)
