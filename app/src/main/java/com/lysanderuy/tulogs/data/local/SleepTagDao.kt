package com.lysanderuy.tulogs.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: SleepTag): Long

    @Delete
    suspend fun delete(tag: SleepTag)

    @Query("SELECT * FROM sleep_tags WHERE type = :type LIMIT 1")
    suspend fun getTagByType(type: TagType): SleepTag?

    @Query("SELECT * FROM sleep_tags")
    fun getAllTags(): Flow<List<SleepTag>>
}