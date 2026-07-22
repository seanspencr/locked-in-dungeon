package com.example.lockedindungeon.data.local

import androidx.room.TypeConverter
import com.example.lockedindungeon.data.local.entities.BlockingType
import com.example.lockedindungeon.data.local.entities.TargetType

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

    @TypeConverter
    fun toTargetType(value : TargetType) : String{
        return value.name
    }

    fun toTargetType(value : String) : TargetType{
        try{
            return TargetType.valueOf(value)
        }catch (e : Exception){
            return TargetType.APP
        }
    }
}