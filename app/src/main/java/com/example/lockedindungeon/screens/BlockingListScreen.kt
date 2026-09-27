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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lockedindungeon.components.TitleBar
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.viewmodels.BlockingListViewModel
import com.example.lockedindungeon.viewmodels.HomeViewModel
import com.example.lockedindungeon.viewmodels.PackageInfoAndBlockingDetail
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun BlockingListScreen(
    viewModel : BlockingListViewModel = hiltViewModel(),
    navigateToBlockingConfigurationScreen : (String) -> Unit
){
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            items(state.blockingDetailList.filter { it.blocking.targetType == state.targetTypeFilter }){
                BlockingCard(it, { packageName -> navigateToBlockingConfigurationScreen(packageName) })
            }
        }

        FloatingActionButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new"
            )
        }
    }
}

@Composable
fun BlockingCard(detail: PackageInfoAndBlockingDetail, onManage : (String) -> Unit){
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)  // outer gap
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(10.dp)
        ,
        Arrangement.SpaceBetween,
        Alignment.CenterVertically,

        ) {


        detail.info.icon?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "${detail.info.packageName} Icon"
            )
        }
        Text(text = detail.info.displayName, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Text(text = detail.blocking.blockingType.toString(), color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Button(onClick = {onManage(detail.info.packageName)}) {
            Text("Manage")
        }
    }
}