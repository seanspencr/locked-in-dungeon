package com.example.lockedindungeon.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.entities.BlockingType
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.local.repositories.AppListRepository
import com.example.lockedindungeon.data.local.repositories.AppBlockingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BlockingConfigurationScreenState(
    val packageName: String = "",
    val displayName: String = "",
    val blockingType: BlockingType = BlockingType.TIMER,
    val targetType: TargetType = TargetType.APP,
    val hour: Int = 0,
    val minute: Int = 0,
    val endHour: Int = 0,
    val endMinute: Int = 0,
    val isSaving: Boolean = false
)

@HiltViewModel
class BlockingConfigurationViewmodel @Inject constructor(
    @ApplicationContext val appContext: Context,
    private val repository: AppBlockingRepository,
    private val appListRepository: AppListRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BlockingConfigurationScreenState())
    val state: StateFlow<BlockingConfigurationScreenState> = _state

    fun setApp(packageName: String, targetType: TargetType) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing : AppBlockingDetail? = repository.findBlockingDetail(packageName)?.firstOrNull()
            if(existing != null){
                _state.value = _state.value.copy(
                    packageName = packageName,
                    displayName = existing.displayName,
                    targetType = existing.targetType,
                    blockingType = existing.blockingType,
                    hour = if (existing.blockingType == BlockingType.TIMER) (existing.timerDurationMinute ?: 0) / 60 else (existing?.startHour ?: 0),
                    minute = if (existing.blockingType == BlockingType.TIMER) (existing.timerDurationMinute ?: 0) % 60 else (existing?.startMinute ?: 0),
                    endHour = existing.endHour ?: 0,
                    endMinute = existing.endMinute ?: 0
                )
            }else{
                _state.value = _state.value.copy(
                    packageName = packageName,
                    displayName = appListRepository.queryPackageInformation(packageName)?.displayName ?: packageName,
                    targetType = targetType,
                    blockingType = BlockingType.BLACKLIST,
                    hour = 0,
                    minute = 0,
                    endHour = 0,
                    endMinute = 0
                )
            }
        }
    }

    fun onBlockingTypeChange(type: BlockingType) {
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
            val detail = AppBlockingDetail(
                packageNameOrUrl = _state.value.packageName,
                displayName = _state.value.displayName,
                blockingType = _state.value.blockingType,
                targetType = _state.value.targetType,
                timerDurationMinute = if (_state.value.blockingType == BlockingType.TIMER) _state.value.hour * 60 + _state.value.minute else null,
                startHour = if (_state.value.blockingType != BlockingType.TIMER) _state.value.hour else null,
                startMinute = if (_state.value.blockingType != BlockingType.TIMER) _state.value.minute else null,
                endHour = if (_state.value.blockingType != BlockingType.TIMER) _state.value.endHour else null,
                endMinute = if (_state.value.blockingType != BlockingType.TIMER) _state.value.endMinute else null
            )
            repository.upsertBlockingDetail(detail)
            _state.value = _state.value.copy(isSaving = false)

            withContext(Dispatchers.Main){
                onComplete()
            }
        }
    }

    fun delete(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBlockingDetail(state.value.packageName)

            withContext(Dispatchers.Main){
                onComplete.invoke()
            }
        }
    }
}
