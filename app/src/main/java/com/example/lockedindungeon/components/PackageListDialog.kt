package com.example.lockedindungeon.components

import android.widget.Spinner
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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

@Composable
fun PackageListDialog(
    viewModel: PackageListViewModel = hiltViewModel(),
    onDismiss : () -> Unit = {}
) {
    val appList = viewModel.state.value.appList

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
            if(viewModel.state.value.isLoading){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }else{
                LazyColumn(
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(
                        items = appList,
                        key = { it.packageName } // stable key, see below
                    ) { app ->

                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
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
                            Text(text = app.displayName)
                            Button({}) {
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