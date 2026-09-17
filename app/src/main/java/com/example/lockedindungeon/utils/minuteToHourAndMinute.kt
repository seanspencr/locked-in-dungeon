package com.example.lockedindungeon.utils

public fun minuteToHourAndMinute(minute : Int) : Pair<Int?, Int>{
    var hour : Int? = minute/60
    hour?.let {
        if(hour <= 0){
            hour = null
        }
    }
    return Pair<Int?, Int>( hour  , minute%60)
}