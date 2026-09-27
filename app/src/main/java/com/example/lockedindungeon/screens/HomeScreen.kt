package com.example.lockedindungeon.screens

 import android.util.Log
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.layout.Arrangement
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
 import androidx.compose.runtime.remember
 import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
 import androidx.compose.runtime.mutableStateOf
 import androidx.compose.runtime.setValue
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.text.style.TextAlign
 import androidx.lifecycle.compose.collectAsStateWithLifecycle
 import com.dotlottie.dlplayer.OpenUrlPolicy
 import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
 import com.lottiefiles.dotlottie.core.compose.runtime.DotLottieController
 import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.example.lockedindungeon.components.NfcDialog
import com.example.lockedindungeon.components.WriteTagPasswordDialog
import com.example.lockedindungeon.viewmodels.HomeViewModel
 import com.lottiefiles.dotlottie.core.util.DotLottieEventListener

@Composable
fun HomeScreen(
    modifier : Modifier = Modifier,
    onNavigate : () -> Unit = {},
    viewModel : HomeViewModel = hiltViewModel())
{
    val controller = remember { DotLottieController() }
    var isAnimationLoaded by remember { mutableStateOf(false) }
    var isStateMachineStarted by remember { mutableStateOf(false) }
    val lottieOnLoadListener = remember {
        object : DotLottieEventListener {
            override fun onLoad() { isAnimationLoaded = true }
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(isAnimationLoaded) {
        if (isAnimationLoaded) {
             val loaded = controller.stateMachineLoad("StateMachine1")
           Log.d("HomeScreen", "Statemachine loaded : ${loaded.toString()}")
            if (loaded) {
                val started = controller.stateMachineStart(
                    OpenUrlPolicy(requireUserInteraction = false, whitelist = listOf("*"))
                )
                Log.d("HomeScreen", "Started: $started") // check this specifically
                isStateMachineStarted = started
            }
        }
    }

    LaunchedEffect(isStateMachineStarted, state.isBlockActive) {
        if (isStateMachineStarted) {
            controller.stateMachineSetBooleanInput("is_active", viewModel.state.value.isBlockActive)
            Log.d("HomeScreen", "Statemachine toggled")
        }
    }


    Column(
        Modifier.padding(48.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

//        Text(text = viewModel.state.value.message, modifier = modifier)

        Text(text = "${state.currentDayStreak}", modifier = modifier, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
        Text(text = "Day Streak", modifier = modifier, textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineSmall)


        Box(
            Modifier.fillMaxSize().weight(1f),
            Alignment.Center
        ){

            DotLottieAnimation(
                source = DotLottieSource.Asset("switch-toggle.lottie"),
                controller = controller,
                eventListeners = listOf(lottieOnLoadListener),
                modifier = Modifier
                    .size(100.dp)
                    .clickable(onClick = { viewModel.toggleBlockActive() })
            )
            Text(text = when(state.isBlockActive){
                true -> "Tap card to disable"
                false -> "Tap card to enable"
            }, modifier = modifier)

        }


//        Button(onClick = {viewModel.setPackageListOpenState(true)}) {
//            Text(text = "Open package list dialog")
//        }

//        Button(onClick = { viewModel.setNfcDialogOpenState(true); enableNfc() }) {
//            Text(text = when{
//                viewModel.state.value.isBlockActive -> "Turn off blocking"
//                else -> "Turn on blocking"
//            })
//        }

        Button(onClick = { viewModel.setIsWritePasswordDialogOpen(true)}) {
            Text(text = "Register new card")
        }

        if(state.isNfcDialogOpen){
            NfcDialog(
                passwordBuffer = state.nfcPasswordBuffer,
                message = state.nfcMessage,
                onPasswordChanged = { viewModel.onNfcPasswordChanged(it) },
                onPasswordSubmit = { viewModel.onNfcPasswordSubmit() },
                onDismiss = { viewModel.dismissNfcDialog() }
            )
        }

        if(state.isWritePasswordDialogOpen) {
            WriteTagPasswordDialog(
                dialogState = state.dialogState,
                writeBuffer = state.writeBuffer ?: "",
                isTagDetected = state.isTagDetected,
                message = state.writeTagMessage,
                errorMessage = state.writeTagErrorMessage,
                onPasswordChanged = { viewModel.onWriteBufferChanged(it) },
                onPasswordSubmit = { viewModel.onWritePasswordSubmit() },
                onDismiss = { viewModel.setIsWritePasswordDialogOpen(false) }
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun HomePreview(){
    HomeScreen()

}