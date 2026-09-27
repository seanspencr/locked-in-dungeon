package com.example.lockedindungeon.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lockedindungeon.components.BlockingCard
import com.example.lockedindungeon.components.InputBlockingUrlDialog
import com.example.lockedindungeon.components.TitleBar
import com.example.lockedindungeon.components.UnblockedPackageListDialog
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.model.PackageInformationDto
import com.example.lockedindungeon.viewmodels.BlockingListViewModel
import com.example.lockedindungeon.viewmodels.HomeViewModel
import com.example.lockedindungeon.viewmodels.PackageInfoAndBlockingDetail
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun BlockingListScreen(
    viewModel : BlockingListViewModel = hiltViewModel(),
    navigateToBlockingConfigurationScreen : (String, TargetType) -> Unit = {str, type -> {}}
){
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isDialogOpen by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleBar(text = "Your Blocked Apps")
        SingleChoiceSegmentedButtonRow() {
            TargetType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = index == state.targetTypeFilter.ordinal,
                    onClick = { viewModel.changeTargetTypeFilter(TargetType.entries[index]) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = TargetType.entries.size)
                ) {
                    Text(type.toString())
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(20.dp)) {
            items(state.blockingList.filter { it.targetType == state.targetTypeFilter }){
                blocking -> val packageInfo = state.appList.firstOrNull{info -> info.packageName == blocking.packageNameOrUrl}
                BlockingCard(
                    blocking.packageNameOrUrl,
                    blocking.displayName,
                    icon = packageInfo?.icon,
                    blockingType = blocking.blockingType,
                    onManage = { packageName, type -> navigateToBlockingConfigurationScreen(packageName, type) },
                    targetType = TargetType.APP
                )
            }
        }

        FloatingActionButton(onClick = {isDialogOpen = true}) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new"
            )
        }
    }

    if(isDialogOpen){
        when(state.targetTypeFilter){
            TargetType.APP -> {
                UnblockedPackageListDialog(
                    onDismissRequest = {isDialogOpen = false},
                    onBlockBtnClick = { packageName, type -> navigateToBlockingConfigurationScreen(packageName, type) },
                    unblockedPackageList = state.appList.filter { info -> state.blockingList.firstOrNull{ blocking -> blocking.packageNameOrUrl == info.packageName } == null  }
                )
            }
            TargetType.URL -> {
                InputBlockingUrlDialog(
                    onDismissRequest = { isDialogOpen = false },
                    onSubmitClicked = {url, type -> navigateToBlockingConfigurationScreen(url, type )}
                )
            }
        }

    }
}