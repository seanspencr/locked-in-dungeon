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
    val blockingDetailList: List<PackageInfoAndBlockingDetail> = listOf(),
    val appList : List<PackageInformationDto> = listOf()
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


                val result =  mutableListOf<PackageInfoAndBlockingDetail>()
                blockingList?.forEach {
                    block ->
                    val packageInfo = appList.firstOrNull{block.packageNameOrUrl == it.packageName}
                    packageInfo?.let{
                        result.add(PackageInfoAndBlockingDetail(block, packageInfo))
                    }
                }

                _state.value = _state.value.copy(
                    blockingDetailList = result,
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