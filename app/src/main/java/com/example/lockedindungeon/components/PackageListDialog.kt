package com.example.lockedindungeon.components

import android.widget.Spinner
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lockedindungeon.viewmodels.PackageListViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lockedindungeon.data.local.entities.TargetType

@Composable
fun PackageListDialog(
    viewModel: PackageListViewModel = hiltViewModel(),
    onDismiss : () -> Unit = {},
    onAppSelected: (packageName: String, displayName: String, targetType: TargetType) -> Unit
) {
    val state = viewModel.state.value
    val appList = state.appList
    val urlList = state.urlList

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
        ){
            Column {
                TabRow(selectedTabIndex = if (state.selectedTargetType == TargetType.APP) 0 else 1) {
                    Tab(
                        selected = state.selectedTargetType == TargetType.APP,
                        onClick = { viewModel.onTargetTypeChange(TargetType.APP) },
                        text = { Text("Apps") }
                    )
                    Tab(
                        selected = state.selectedTargetType == TargetType.URL,
                        onClick = { viewModel.onTargetTypeChange(TargetType.URL) },
                        text = { Text("URLs") }
                    )
                }

                if(state.isLoading){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }else{
                    Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                        if (state.selectedTargetType == TargetType.URL) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = state.urlInput,
                                    onValueChange = { viewModel.onUrlInputChange(it) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Enter URL") }
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { onAppSelected(state.urlInput, state.urlInput, TargetType.URL) }) {
                                    Text("Submit")
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val itemsToShow = if (state.selectedTargetType == TargetType.APP) appList else urlList
                            items(
                                items = itemsToShow,
                                key = { it.packageName }
                            ) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (state.selectedTargetType == TargetType.APP) {
                                        val iconDrawable =
                                            LocalContext.current.packageManager.getApplicationIcon(app.packageName)
                                        val iconBitmap = iconDrawable.toBitmap(60, 60)
                                        Image(
                                            bitmap = iconBitmap.asImageBitmap(),
                                            contentDescription = "App Icon",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                    
                                    Text(
                                        text = app.displayName,
                                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                                    )
                                    Button(onClick = { onAppSelected(app.packageName, app.displayName, state.selectedTargetType) }) {
                                        Text(when(app.isBlocked){
                                            true -> "Manage"
                                            false -> "Block"
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}