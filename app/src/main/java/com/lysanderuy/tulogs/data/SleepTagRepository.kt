package com.lysanderuy.tulogs.data

import com.lysanderuy.tulogs.data.local.SleepTag
import com.lysanderuy.tulogs.data.local.SleepTagDao
import com.lysanderuy.tulogs.data.local.TagType
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTagRepository @Inject constructor(
    private val sleepTagDao: SleepTagDao,
    private val authRepository: AuthRepository
) {
    val allTags: Flow<List<SleepTag>> = authRepository.currentUserIdFlow.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else sleepTagDao.getAllTags(userId)
    }

    suspend fun registerTag(uid: String, type: TagType) {
        val userId = authRepository.currentUserId ?: return
        sleepTagDao.insert(SleepTag(userId = userId, uid = uid, type = type))
    }

    suspend fun getTagByType(type: TagType): SleepTag? {
        val userId = authRepository.currentUserId ?: return null
        return sleepTagDao.getTagByType(type, userId)
    }
}