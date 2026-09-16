package com.example.lockedindungeon.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.lockedindungeon.viewmodels.BlockingListViewModel
import com.example.lockedindungeon.viewmodels.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun BlockingListScreen(
    onNavigate : ()->Unit = {},
    viewModel : BlockingListViewModel = hiltViewModel()
){
    Text(
        text = "Navigated"
    )
    Button(onClick = onNavigate) {
        Text(text = "Navigate back")
    }
}