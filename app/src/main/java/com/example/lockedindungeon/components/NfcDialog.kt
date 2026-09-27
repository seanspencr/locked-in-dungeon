package com.example.lockedindungeon.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun NfcDialog(
    passwordBuffer: String,
    message: String?,
    onPasswordChanged: (String) -> Unit,
    onPasswordSubmit: () -> Unit,
    onDismiss : () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {

                Text(
                    "Please enter your password",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                message?.let {
                    Text(it, textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(24.dp))

                TextField(
                    value = passwordBuffer,
                    onValueChange = onPasswordChanged,
                    label = { Text("Password") }
                )

                Spacer(Modifier.height(24.dp))

                Button(onPasswordSubmit) {
                    Text("Submit")
                }
            }
        }
    }
}
