package com.example.lockedindungeon.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Asks the user to turn notifications on. UI only: the caller decides what to do with the answer,
 * for example launching the POST_NOTIFICATIONS request on API 33+ or showing a settings deep link.
 *
 * @param onAccept called with `true` when the user says yes, `false` when the user says no.
 * The dialog is expected to be closed by the caller after either answer.
 * @param onDismiss called when the dialog is dismissed without answering (tap outside / back).
 */
@Composable
fun NotificationRequestDialog(
    onDismiss : ()-> Unit = {},
    onAccept : (Boolean) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                TitleBar(text = "Turn on notifications")

                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    "Please enable notifications for the optimal experience. " +
                        "We use them to remind you that your block is still running, " +
                        "so a slip is harder to hide from yourself.",
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onAccept(true) },
                        modifier = Modifier.weight(4f),
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Yes")
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = { onAccept(false) },
                        modifier = Modifier.weight(5f),
                        colors = ButtonColors(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                    ) {
                        Text("No thanks")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationRequestDialogPreview() {
    NotificationRequestDialog()
}
