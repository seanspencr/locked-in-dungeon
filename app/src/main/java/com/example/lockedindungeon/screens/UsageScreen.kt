package com.example.lockedindungeon.screens

import android.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.icu.text.CaseMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.lockedindungeon.components.TitleBar
import com.example.lockedindungeon.data.local.repositories.UsageStatsMinute
import com.example.lockedindungeon.data.model.PackageInformationDto
import com.example.lockedindungeon.utils.minuteToHourAndMinute
import com.example.lockedindungeon.viewmodels.UsageViewModel

@Composable
fun UsageScreen(
    viewmodel : UsageViewModel = hiltViewModel(),
    modifier : Modifier = Modifier,
    navigateToBlockingConfigurationScreen : (String)->Unit = {}
    ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleBar(text = "Your App Usage")

        LazyColumn(contentPadding = PaddingValues(20.dp)){
            items(viewmodel.state.value.appUsage.filter { it -> it.usageMinute > 0 }.sortedBy { -it.usageMinute }){ usageData ->
                UsageCard(usageData = usageData, onClick  = navigateToBlockingConfigurationScreen)
            }
        }
    }

}

@Composable
fun UsageCard(usageData : UsageStatsMinute = UsageStatsMinute(usageMinute = 67, packageInfo = PackageInformationDto(
    "testing", "testing"
)), onClick : (String)->Unit = {}){

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
            .clickable(enabled = true)
            {
                onClick(usageData.packageInfo.packageName)
            }        ,
        Arrangement.SpaceBetween,
        Alignment.CenterVertically,

    ) {


        usageData.packageInfo.icon?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "${usageData.packageInfo.packageName} Icon"
            )
        }
        Text(text = usageData.packageInfo.displayName, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Badge( containerColor = badgeColors.first) {
            Text(
                text = "${usageHourMinute.first?.let{ "${it}h" } ?: ""} ${usageHourMinute.second}m",
                color =  badgeColors.second,
                modifier = Modifier.padding(4.dp))
        }
    }

}

