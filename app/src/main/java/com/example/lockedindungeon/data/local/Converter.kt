package com.example.lockedindungeon.data.local

import androidx.room.TypeConverter
import com.example.lockedindungeon.data.local.entities.BlockingType

class Converters {
    @TypeConverter
    fun fromBlockingType(value: BlockingType): String {
        return value.name
    }

    @TypeConverter
    fun toBlockingType(value: String): BlockingType {
        try{
            return BlockingType.valueOf(value)
        }catch (e : Exception){
            return BlockingType.TIMER
        }
    }
}