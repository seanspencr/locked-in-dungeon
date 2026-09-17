package com.example.lockedindungeon.screens

import android.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.icu.text.CaseMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.lockedindungeon.data.local.repositories.UsageStatsMinute
import com.example.lockedindungeon.utils.minuteToHourAndMinute
import com.example.lockedindungeon.viewmodels.UsageViewModel

@Composable
fun UsageScreen(viewmodel : UsageViewModel = hiltViewModel(), modifier : Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Your App Usage",
        )
        LazyColumn(contentPadding = PaddingValues(20.dp)){
            items(viewmodel.state.value.appUsage.filter { it -> it.usageMinute > 0 }.sortedBy { -it.usageMinute }){ usageData ->
                UsageCard(usageData)
            }
        }
    }

}

@Composable
fun UsageCard(usageData : UsageStatsMinute = UsageStatsMinute(packageName =
"testing", usageMinute = 67)){

    val icon : Bitmap = LocalContext.current.packageManager.getApplicationIcon(usageData.packageName).toBitmap(120, 120 )
//    TODO : fetch app name

    val usageHourMinute = minuteToHourAndMinute(usageData.usageMinute)

    Row(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
        ,
        Arrangement.SpaceBetween
    ) {
        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = "${usageData.packageName} Icon"
        )
        Text(text = usageData.packageName)

        Text(text = "${usageHourMinute.first?.let{ "${it}h" } ?: ""} ${usageHourMinute.second}m")
    }

}

