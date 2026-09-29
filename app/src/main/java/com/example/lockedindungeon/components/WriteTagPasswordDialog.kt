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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lockedindungeon.viewmodels.DialogState
import com.example.lockedindungeon.viewmodels.WriteTagViewModel


@Composable
fun WriteTagPasswordDialog(
    onDismiss : () -> Unit,
    viewModel : WriteTagViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        DialogProperties(usePlatformDefaultWidth = false)
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
        ){


            Column(
                modifier = Modifier.padding(24.dp),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {

                when(state.dialogState) {
                    DialogState.INPUT_PASSWORD -> {

                        Text("Set your card password", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(18.dp))
                        Text("you will be asked for this password when you try to disable app blocking with your card", textAlign = TextAlign.Left)

                        Spacer(Modifier.height(24.dp))
                        TextField(
                            value = state.writeBuffer ?: "",
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            label = { Text("Password") }
                        )
                        Text(
                            text = state.errorMessage ?: "",
                        )
                        Button(onClick = { viewModel.onPasswordSubmit() }) {
                            Text("Submit your password")
                        }

                    }
                    DialogState.WRITE_TAG -> {

                        Text("Register your card", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(18.dp))

                        if(state.isTagDetected){
                            Text("Data successfully written into NFC card")
                            Text(state.message ?: "")
                        }else{
                            Text("Please tap your nfc card to write the data into the card")
                        }


                    }
                }
            }
        }
    }
}
