package com.example.lockedindungeon.components

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.lockedindungeon.viewmodels.NfcScanDialogState
import com.example.lockedindungeon.viewmodels.NfcScanViewmodel

@Composable
fun NfcDialog(
    viewModel: NfcScanViewmodel = hiltViewModel(),
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
            when(viewModel.state.value.dialogState){
                NfcScanDialogState.READ_CARD -> {
                    if(viewModel.state.value.isDetected){
                        Column() {
                            Text("Nfc detected")
                            Text("Message: ${viewModel.state.value.message}")
                        }
                    }else{
                        Box(){
                            Text("Please tap your nfc card")
                        }
                    }
                }
                NfcScanDialogState.CHECK_PASSWORD -> {
                    Column() {
                        Text("Please enter your password")
                        Text("Message: ${viewModel.state.value.message}")
                        TextField(value = viewModel.state.value.passwordBuffer, onValueChange = { viewModel.onWriteBufferChanged(it) }, label = {Text("Password")    })
                        Button({ viewModel.onPasswordSubmit() }) {
                            Text("Submit")
                        }
                    }
                }
            }

        }
    }
}