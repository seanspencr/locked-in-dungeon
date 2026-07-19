package com.example.lockedindungeon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocking_detail")
data class PackageBlockingDetail(

    @PrimaryKey val packageName : String,
    val displayName : String = "",
    val blockingType : String = "",
    val timerDurationMinute : Int?,
    val startHour : Int?,
    val startMinute: Int?,
    val endHour : Int?,
    val endMinute : Int?
)
