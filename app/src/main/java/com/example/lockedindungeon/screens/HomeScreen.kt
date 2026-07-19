package com.example.lockedindungeon.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.lockedindungeon.components.NfcDialog
import com.example.lockedindungeon.components.PackageListDialog
import com.example.lockedindungeon.components.WriteTagPasswordDialog
import com.example.lockedindungeon.viewmodels.HomeViewModel

@Composable
fun HomeScreen(modifier : Modifier = Modifier, viewModel : HomeViewModel = hiltViewModel(), enableNfc : ()-> Unit = {}, disableNfc : ()-> Unit = {}) {

    Column() {
        Text(text = viewModel.homeState.value.message, modifier = modifier)

        Button(onClick = {
            viewModel.changeMessage("I am changed")
        }) {
            Text(text = "Change your mama")
        }
        Button(onClick = {viewModel.setPackageListOpenState(true)}) {
            Text(text = "Open package list dialog")
        }

        Button(onClick = { viewModel.setNfcDialogOpenState(true); enableNfc() }) {
            Text(text = when{
                viewModel.homeState.value.isBlockActive -> "Turn off blocking"
                else -> "Turn on blocking"
            })
        }
        Button(onClick = { viewModel.setWritePasswordDialogOpenState(true); enableNfc() }) {
            Text(text = "Register new card")
        }

        if (viewModel.homeState.value.isPackageListDialogOpen) {
            PackageListDialog(onDismiss = { viewModel.setPackageListOpenState(false) })
        }

        if(viewModel.homeState.value.isNfcDialogOpen){
            NfcDialog(onDismiss = { viewModel.setNfcDialogOpenState(false); disableNfc() })
        }

        if(viewModel.homeState.value.isWritePasswordDialogOpen) {
            WriteTagPasswordDialog(onDismiss = { viewModel.setWritePasswordDialogOpenState(false); disableNfc() })
        }
    }

}

@Preview(showBackground = true)
@Composable
fun HomePreview(){
    HomeScreen()

}