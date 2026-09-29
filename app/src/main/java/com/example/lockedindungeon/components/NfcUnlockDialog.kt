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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lockedindungeon.viewmodels.NfcUnlockDialogStateEnum
import com.example.lockedindungeon.viewmodels.NfcUnlockDialogViewmodel

@Composable
fun NfcUnlockDialog(
    viewModel: NfcUnlockDialogViewmodel = viewModel(),
    onDismiss: () -> Unit,
    onDisableSuccess: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    var inputPassword by remember { mutableStateOf("") }
    var inputDisableAttemptWord by remember { mutableStateOf("") }
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

                when(state.state){
                    NfcUnlockDialogStateEnum.INPUT_PASSWORD -> {

                        Text(
                            "Please enter your password",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(18.dp))

                        state.passwordInputMessage?.let {
                            Text(it, textAlign = TextAlign.Center)
                        }

                        Spacer(Modifier.height(24.dp))

                        TextField(
                            value = inputPassword,
                            onValueChange = { inputPassword = it },
                            label = { Text("Password") }
                        )

                        Spacer(Modifier.height(24.dp))

                        Button({
                            viewModel.submitPassword(
                                inputPassword = inputPassword,
                                onSuccess = {
                                    viewModel.continueState(onDisableSuccess)
                                }
                            )
                        }) {
                            Text("Submit")
                        }



                    }
                    NfcUnlockDialogStateEnum.INPUT_DISABLE_ATTEMPT_WORD -> {
                        TitleBar(text = "Do me a favor")


                        Text(
                            "Please fill exactly like the hint",
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(18.dp))

                        state.disableAttemptMessage?.let {
                            Text(it, textAlign = TextAlign.Center)
                        }

                        Spacer(Modifier.height(24.dp))

                        TextField(
                            value = inputDisableAttemptWord,
                            onValueChange = { inputDisableAttemptWord = it },
                            placeholder = { state.disableAttemptWord?.let { Text(it) } },
                            label = {Text("For unblocking the apps i commited to block, I declare that I am a")},
                        )

                        Spacer(Modifier.height(24.dp))

                        Button({
                            viewModel.submitDisableAttemptWord(
                                inputAttempt = inputDisableAttemptWord,
                                onSuccess = { viewModel.continueState(onDismiss) }
                            )
                        }) {
                            Text("Declare")
                        }
                    }
                }
            }
        }
    }
}
