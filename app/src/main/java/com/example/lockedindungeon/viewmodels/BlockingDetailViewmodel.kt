package com.example.lockedindungeon.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.entities.PackageBlockingDetail
import com.example.lockedindungeon.data.local.repositories.PackageBlockingLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BlockingDetailState(
    val packageName: String = "",
    val displayName: String = "",
    val blockingType: String = "Blacklist",
    val hour: Int = 0,
    val minute: Int = 0,
    val endHour: Int = 0,
    val endMinute: Int = 0,
    val isSaving: Boolean = false
)

@HiltViewModel
class BlockingDetailViewmodel @Inject constructor(
    private val repository: PackageBlockingLocalRepository
) : ViewModel() {
    private val _state = mutableStateOf(BlockingDetailState())
    val state: State<BlockingDetailState> = _state

    fun setApp(packageName: String, displayName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.findBlockingDetail(packageName)
            _state.value = _state.value.copy(
                packageName = packageName,
                displayName = displayName,
                blockingType = existing?.blockingType ?: "Blacklist",
                hour = if (existing?.blockingType == "Timer") (existing.timerDurationMinute ?: 0) / 60 else (existing?.startHour ?: 0),
                minute = if (existing?.blockingType == "Timer") (existing.timerDurationMinute ?: 0) % 60 else (existing?.startMinute ?: 0),
                endHour = existing?.endHour ?: 0,
                endMinute = existing?.endMinute ?: 0
            )
        }
    }

    fun onBlockingTypeChange(type: String) {
        _state.value = _state.value.copy(blockingType = type)
    }

    fun onHourChange(hour: Int) {
        _state.value = _state.value.copy(hour = hour)
    }

    fun onMinuteChange(minute: Int) {
        _state.value = _state.value.copy(minute = minute)
    }

    fun onEndHourChange(hour: Int) {
        _state.value = _state.value.copy(endHour = hour)
    }

    fun onEndMinuteChange(minute: Int) {
        _state.value = _state.value.copy(endMinute = minute)
    }

    fun submit(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isSaving = true)
            val detail = PackageBlockingDetail(
                packageName = _state.value.packageName,
                displayName = _state.value.displayName,
                blockingType = _state.value.blockingType,
                timerDurationMinute = if (_state.value.blockingType == "Timer") _state.value.hour * 60 + _state.value.minute else null,
                startHour = if (_state.value.blockingType != "Timer") _state.value.hour else null,
                startMinute = if (_state.value.blockingType != "Timer") _state.value.minute else null,
                endHour = if (_state.value.blockingType != "Timer") _state.value.endHour else null,
                endMinute = if (_state.value.blockingType != "Timer") _state.value.endMinute else null
            )
            repository.insertBlockingDetail(detail)
            _state.value = _state.value.copy(isSaving = false)
            onComplete()
        }
    }
}
