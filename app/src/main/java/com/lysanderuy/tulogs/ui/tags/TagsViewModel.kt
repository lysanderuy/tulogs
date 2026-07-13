package com.lysanderuy.tulogs.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lysanderuy.tulogs.data.SleepTagRepository
import com.lysanderuy.tulogs.data.local.TagType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TagsUiState(
    val bedtimeUid: String?,
    val wakeUid: String?
)

@HiltViewModel
class TagsViewModel @Inject constructor(
    sleepTagRepository: SleepTagRepository
) : ViewModel() {

    val uiState: StateFlow<TagsUiState> = sleepTagRepository.allTags
        .map { tags ->
            TagsUiState(
                bedtimeUid = tags.firstOrNull { it.type == TagType.BEDTIME }?.uid,
                wakeUid = tags.firstOrNull { it.type == TagType.WAKE }?.uid
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TagsUiState(bedtimeUid = null, wakeUid = null)
        )
}
