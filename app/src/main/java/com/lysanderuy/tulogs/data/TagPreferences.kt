package com.lysanderuy.tulogs.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tag_prefs")

class TagPreferences(private val context: Context) {

    companion object {
        private val BEDTIME_TAG_UID = stringPreferencesKey("bedtime_tag_uid")
        private val WAKE_TAG_UID = stringPreferencesKey("wake_tag_uid")
    }

    val bedtimeTagUid: Flow<String?> = context.dataStore.data.map { it[BEDTIME_TAG_UID] }
    val wakeTagUid: Flow<String?> = context.dataStore.data.map { it[WAKE_TAG_UID] }

    suspend fun setBedtimeTag(uid: String) {
        context.dataStore.edit { it[BEDTIME_TAG_UID] = uid }
    }

    suspend fun setWakeTag(uid: String) {
        context.dataStore.edit { it[WAKE_TAG_UID] = uid }
    }
}