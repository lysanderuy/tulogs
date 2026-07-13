package com.lysanderuy.tulogs.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromTagType(type: TagType): String = type.name

    @TypeConverter
    fun toTagType(value: String): TagType = TagType.valueOf(value)
}