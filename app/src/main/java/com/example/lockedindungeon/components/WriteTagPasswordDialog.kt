package com.example.lockedindungeon.components

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.lockedindungeon.viewmodels.DialogState
import com.example.lockedindungeon.viewmodels.NfcScanViewmodel
import com.example.lockedindungeon.viewmodels.WriteTagPasswordViewmodel


@Composable
fun WriteTagPasswordDialog(
    viewModel: WriteTagPasswordViewmodel = hiltViewModel(),
    onDismiss : () -> Unit = {}
) {
    Dialog(onDismissRequest = {
        onDismiss()
        viewModel.reset()
    }){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ){

            when(viewModel.state.value.dialogState ) {
                DialogState.INPUT_PASSWORD -> {
                    Column() {
                        Text("Assign a password for your tag (you will be asked for this password when you try to disable app blocking)")
                        TextField(
                            value = viewModel.state.value.writeBuffer ?: "",
                            onValueChange = { viewModel.onWriteBufferChanged(it) },
                            label = { Text("Password") }
                        )
                        Text(
                            text = viewModel.state.value.errorMessage ?: "",
                        )
                        Button({ viewModel.onPasswordSubmit() }) {
                            Text("Submit your password")
                        }
                    }
                }
                DialogState.WRITE_TAG -> {
                    if(viewModel.state.value.isDetected){
                        Text("NFC card detected successfully")
                        Text(viewModel.state.value.message ?: "")
                    }else{
                        Text("Please tap your nfc card")
                    }
                }
            }

        }
    }
}