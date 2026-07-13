package com.lysanderuy.tulogs.data

import com.lysanderuy.tulogs.data.local.SleepTag
import com.lysanderuy.tulogs.data.local.SleepTagDao
import com.lysanderuy.tulogs.data.local.TagType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SleepTagRepository @Inject constructor(
    private val sleepTagDao: SleepTagDao
) {
    val allTags: Flow<List<SleepTag>> = sleepTagDao.getAllTags()

    suspend fun registerTag(uid: String, type: TagType) {
        sleepTagDao.insert(SleepTag(uid = uid, type = type))
    }

    suspend fun getTagByType(type: TagType): SleepTag? {
        return sleepTagDao.getTagByType(type)
    }
}