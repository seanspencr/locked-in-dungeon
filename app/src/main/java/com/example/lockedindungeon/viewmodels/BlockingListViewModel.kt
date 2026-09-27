package com.example.lockedindungeon.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.local.repositories.AppBlockingRepository
import com.example.lockedindungeon.data.local.repositories.AppListRepository
import com.example.lockedindungeon.data.model.PackageInformationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackageInfoAndBlockingDetail(val blocking : AppBlockingDetail, val info : PackageInformationDto)
data class BlockingListScreenState(
    val targetTypeFilter: TargetType = TargetType.APP,
    val appList : List<PackageInformationDto> = listOf(),
    val blockingList : List<AppBlockingDetail> = listOf()
)

@HiltViewModel
class BlockingListViewModel @Inject constructor(
    private val appListRepository: AppListRepository,
    private val blockingListRepository: AppBlockingRepository
) : ViewModel() {

    private val _state : MutableStateFlow<BlockingListScreenState> = MutableStateFlow(BlockingListScreenState())
    public val state : StateFlow<BlockingListScreenState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appListRepository.appList.combine(
                blockingListRepository.selectBlockingDetails() ?: flowOf(null),
                transform = {
                    appList, blockingList -> Pair(appList, blockingList)
                }
            )
            .collect {
                combined ->
                val appList = combined.first
                val blockingList = combined.second

                _state.value = _state.value.copy(
                    blockingList = blockingList ?: listOf(),
                    appList = appList
                )
            }
        }
    }

    fun changeTargetTypeFilter(type : TargetType){
        _state.value = _state.value.copy(
            targetTypeFilter = type
        )
    }

}