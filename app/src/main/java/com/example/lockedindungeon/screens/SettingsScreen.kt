package com.example.lockedindungeon.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lockedindungeon.components.TitleBar
import com.example.lockedindungeon.viewmodels.SettingsViewmodel

@Composable
fun SettingsScreen(viewmodel: SettingsViewmodel = viewModel()) {

    val state by viewmodel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        TitleBar(text = "Settings")

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            SettingCard(
                name = "Disable Attempt Word",
                icon = Icons.Default.Password,
                value = state.appSettings.disableAttemptWord,
                onEditSaved = { viewmodel.saveDisableAttemptWord(it) }
            )
        }
    }
}

@Composable
fun EditStringSettingDialog(initialValue: String, name: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var fieldState by remember { mutableStateOf(initialValue) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                TitleBar(
                    text = "Edit $name",
                    actionLeft = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Cancel"
                            )
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                TextField(
                    value = fieldState,
                    onValueChange = { fieldState = it },
                    label = { Text(name) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonColors(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(onClick = { onSave(fieldState) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Save"
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingCard(name: String, icon: ImageVector, value: String?, onEditSaved: (String) -> Unit) {
    var isDialogOpen by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .clickable { isDialogOpen = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = name
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value ?: "Not set",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Edit $name"
            )
        }
    }

    if (isDialogOpen) {
        EditStringSettingDialog(
            name = name,
            initialValue = value.orEmpty(),
            onDismiss = { isDialogOpen = false },
            onSave = { onEditSaved(it); isDialogOpen = false }
        )
    }
}
