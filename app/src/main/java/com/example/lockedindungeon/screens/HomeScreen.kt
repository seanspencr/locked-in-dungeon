package com.example.lockedindungeon.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lockedindungeon.components.PackageList
import com.example.lockedindungeon.viewmodels.HomeViewModel

@Composable
fun HomeScreen(modifier : Modifier = Modifier, homeViewModel : HomeViewModel = HomeViewModel()) {
    Column() {
        Text(text = homeViewModel.homeState.value.message, modifier = modifier)

        Button(onClick = {
            homeViewModel.changeMessage("I am changed")
        }) {
            Text(text = "Change your mama")
        }

        PackageList()
    }

}

@Preview(showBackground = true)
@Composable
fun HomePreview(){
    HomeScreen()

}