package com.example.lockedindungeon.screens

import android.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.icu.text.CaseMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            Arrangement.Center,
            Alignment.CenterVertically
        ){
            Text(
                text = "Your App Usage",
                style = MaterialTheme.typography.headlineLarge

            )
        }

        LazyColumn(contentPadding = PaddingValues(20.dp)){
            items(viewmodel.state.value.appUsage.filter { it -> it.usageMinute > 0 }.sortedBy { -it.usageMinute }){ usageData ->
                UsageCard(usageData)
            }
        }
    }

}

@Composable
fun UsageCard(usageData : UsageStatsMinute = UsageStatsMinute(packageName =
"testing", displayName = "tetsing", usageMinute = 67)){

    val icon : Bitmap = LocalContext.current.packageManager.getApplicationIcon(usageData.packageName).toBitmap(120, 120 )
//    TODO : fetch app name

    val usageHourMinute = minuteToHourAndMinute(usageData.usageMinute)
    val badgeColors = if (usageHourMinute.first != null)
        Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    else
        Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)




    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)  // outer gap
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(10.dp)
        ,
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {


        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = "${usageData.packageName} Icon"
        )
        Text(text = usageData.displayName, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Badge( containerColor = badgeColors.first) {
            Text(
                text = "${usageHourMinute.first?.let{ "${it}h" } ?: ""} ${usageHourMinute.second}m",
                color =  badgeColors.second,
                modifier = Modifier.padding(4.dp))
        }
    }

}

