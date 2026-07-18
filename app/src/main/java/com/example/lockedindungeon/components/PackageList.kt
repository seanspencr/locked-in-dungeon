package com.example.lockedindungeon.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.TableInfo
import com.example.lockedindungeon.viewmodels.PackageListViewModel

@Composable
fun PackageList(
    viewModel: PackageListViewModel = viewModel()
) {
    val appList = viewModel.state.value.appList

    Column() {
        appList.map {
            Row() {
                Text(text = it.displayName)
            }
        }
    }
}