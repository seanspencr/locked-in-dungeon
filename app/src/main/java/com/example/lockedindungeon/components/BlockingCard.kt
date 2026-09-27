package com.example.lockedindungeon.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lockedindungeon.data.local.entities.AppBlockingDetail
import com.example.lockedindungeon.data.local.entities.BlockingType
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.model.PackageInformationDto

@Composable
fun BlockingCard(
    packageNameOrUrl :  String,
    displayName : String,
    targetType: TargetType = TargetType.APP,
    icon : Bitmap? = null,
    blockingType: BlockingType? = null,
    btnText : String = "Manage",
    onManage : (String, TargetType) -> Unit){
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)  // outer gap
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(10.dp)
        ,
        Arrangement.SpaceBetween,
        Alignment.CenterVertically,
    ) {


        icon?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "$packageNameOrUrl Icon"
            )
        }
        Text(
            text = displayName,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.5f, fill = false)
        )

        blockingType?.let {
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text(text = blockingType.toString(), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Button(onClick = {onManage(packageNameOrUrl, targetType)}) {
            Text(btnText)
        }
    }
}