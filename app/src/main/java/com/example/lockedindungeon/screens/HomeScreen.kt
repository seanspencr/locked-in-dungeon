package com.example.lockedindungeon.screens

 import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
 import androidx.compose.runtime.remember
 import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
 import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
 import com.lottiefiles.dotlottie.core.compose.runtime.DotLottieController
 import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.example.lockedindungeon.components.BlockingDetailDialog
import com.example.lockedindungeon.components.NfcDialog
import com.example.lockedindungeon.components.PackageListDialog
import com.example.lockedindungeon.components.WriteTagPasswordDialog
import com.example.lockedindungeon.viewmodels.HomeViewModel
@Composable
fun HomeScreen(modifier : Modifier = Modifier, viewModel : HomeViewModel = hiltViewModel()) {
    val controller = remember { DotLottieController() }

    LaunchedEffect(viewModel.state.value.isBlockActive) {
        controller.stateMachineSetBooleanInput("is_active", viewModel.state.value.isBlockActive)
    }

    Column() {
        DotLottieAnimation(
            source = DotLottieSource.Asset("switch-toggle.lottie"),
            controller = controller,
            modifier = Modifier
                .size(100.dp)
                .clickable { viewModel.toggleBlockActive() }
        )
        Text(text = viewModel.state.value.message, modifier = modifier)

        Text(text = when(viewModel.state.value.isBlockActive){
            true -> "Tap card to disable"
            false -> "Tap card to enable"
        }, modifier = modifier)

        Button(onClick = {viewModel.setPackageListOpenState(true)}) {
            Text(text = "Open package list dialog")
        }

//        Button(onClick = { viewModel.setNfcDialogOpenState(true); enableNfc() }) {
//            Text(text = when{
//                viewModel.state.value.isBlockActive -> "Turn off blocking"
//                else -> "Turn on blocking"
//            })
//        }

        Button(onClick = { viewModel.setWritePasswordDialogOpenState(true)}) {
            Text(text = "Register new card")
        }

        if (viewModel.state.value.isPackageListDialogOpen) {
            PackageListDialog(
                onDismiss = { viewModel.setPackageListOpenState(false) },
                onAppSelected = { pkg, name, type -> viewModel.openBlockingDetail(pkg, name, type) }
            )
        }

        if (viewModel.state.value.isBlockingDetailDialogOpen) {
            BlockingDetailDialog(
                packageName = viewModel.state.value.selectedPackageName,
                displayName = viewModel.state.value.selectedDisplayName,
                targetType = viewModel.state.value.selectedTargetType,
                onDismiss = { viewModel.closeBlockingDetail() }
            )
        }

        if(viewModel.state.value.isNfcDialogOpen){
            NfcDialog(
                hashedPassword = viewModel.state.value.nfcHashedPassword,
                onDismiss = { viewModel.setNfcDialogOpenState(false)},
                onPasswordMatch = { viewModel.setNfcDialogOpenState(false)}
            )
        }

        if(viewModel.state.value.isWritePasswordDialogOpen) {
            WriteTagPasswordDialog(onDismiss = { viewModel.setWritePasswordDialogOpenState(false)})
        }
    }

}

@Preview(showBackground = true)
@Composable
fun HomePreview(){
    HomeScreen()

}