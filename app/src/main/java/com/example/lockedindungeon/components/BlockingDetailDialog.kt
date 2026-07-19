package com.example.lockedindungeon.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lockedindungeon.viewmodels.BlockingDetailViewmodel

@Composable
fun BlockingDetailDialog(
    packageName: String,
    displayName: String,
    onDismiss: () -> Unit,
    viewModel: BlockingDetailViewmodel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(packageName) {
        viewModel.setApp(packageName, displayName)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val iconDrawable = LocalContext.current.packageManager.getApplicationIcon(packageName)
                val iconBitmap = iconDrawable.toBitmap(80, 80)
                Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                )
                Text(text = displayName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text("Blocking Mode")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Blacklist", "Whitelist", "Timer").forEach { type ->
                        FilterChip(
                            selected = state.blockingType == type,
                            onClick = { viewModel.onBlockingTypeChange(type) },
                            label = { Text(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(if (state.blockingType == "Timer") "Duration" else "Start Time")
                
                Text("${state.hour} hours")
                Slider(
                    value = state.hour.toFloat(),
                    onValueChange = { viewModel.onHourChange(it.toInt()) },
                    valueRange = 0f..23f,
                    steps = 23
                )

                Text("${state.minute} minutes")
                Slider(
                    value = state.minute.toFloat(),
                    onValueChange = { viewModel.onMinuteChange(it.toInt()) },
                    valueRange = 0f..59f,
                    steps = 59
                )

                if (state.blockingType != "Timer") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("End Time")

                    Text("${state.endHour} hours")
                    Slider(
                        value = state.endHour.toFloat(),
                        onValueChange = { viewModel.onEndHourChange(it.toInt()) },
                        valueRange = 0f..23f,
                        steps = 23
                    )

                    Text("${state.endMinute} minutes")
                    Slider(
                        value = state.endMinute.toFloat(),
                        onValueChange = { viewModel.onEndMinuteChange(it.toInt()) },
                        valueRange = 0f..59f,
                        steps = 59
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.submit(onDismiss) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text("Submit")
                }
            }
        }
    }
}
