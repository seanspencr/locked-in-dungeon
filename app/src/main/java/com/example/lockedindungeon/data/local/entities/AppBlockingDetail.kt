package com.example.lockedindungeon.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocking_detail")
data class AppBlockingDetail(

    @PrimaryKey val packageNameOrUrl : String,
    val displayName : String = "",
    val blockingType : BlockingType = BlockingType.TIMER,
    val targetType : TargetType = TargetType.APP,
    val timerDurationMinute : Int?,
    val startHour : Int?,
    val startMinute: Int?,
    val endHour : Int?,
    val endMinute : Int?
)
