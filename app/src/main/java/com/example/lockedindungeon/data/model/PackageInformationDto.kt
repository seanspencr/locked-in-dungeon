package com.example.lockedindungeon.data.model

import android.graphics.Bitmap

data class PackageInformationDto(
    val packageName : String,
    val displayName : String,
    var isBlocked : Boolean = false,
    var icon : Bitmap? = null
)
