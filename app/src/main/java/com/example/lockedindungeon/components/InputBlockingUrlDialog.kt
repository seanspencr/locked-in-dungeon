package com.example.lockedindungeon.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.lockedindungeon.data.local.entities.TargetType

@Composable
fun InputBlockingUrlDialog(
    onDismissRequest : ()-> Unit,
    onSubmitClicked : (String, TargetType) -> Unit
){
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
            ,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier.padding(horizontal = 24.dp).padding(top = 24.dp),
                Arrangement.Center,
                Alignment.CenterHorizontally,

            ) {
                Text("Enter url you want to block")
                Spacer(Modifier.height(18.dp))
                var inputText by remember { mutableStateOf("") }
                TextField(value = inputText, onValueChange = {inputText = it})

                Row(
                    Modifier.padding(24.dp).fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(4f),
                        colors = ButtonColors(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete"
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }


                    Spacer(Modifier.width(10.dp))


                    Button(
                        onClick = {onSubmitClicked(inputText, TargetType.URL)},
                        Modifier.weight(5f),
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Submit"
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("Submit")
                    }
                }

            }
        }
    }
}