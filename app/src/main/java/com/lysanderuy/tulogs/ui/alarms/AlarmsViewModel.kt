package com.lysanderuy.tulogs.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lysanderuy.tulogs.data.AlarmRepository
import com.lysanderuy.tulogs.data.local.Alarm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlarmsUiState(val alarms: List<Alarm> = emptyList())

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    val uiState: StateFlow<AlarmsUiState> = alarmRepository.allAlarms
        .map { alarms -> AlarmsUiState(alarms = alarms.sortedBy { it.hour * 60 + it.minute }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlarmsUiState()
        )

    fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch { alarmRepository.saveAlarm(alarm) }
    }

    fun setEnabled(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch { alarmRepository.setEnabled(alarm, isEnabled) }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch { alarmRepository.deleteAlarm(alarm) }
    }
}
