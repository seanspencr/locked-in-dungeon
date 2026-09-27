package com.example.lockedindungeon.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.data.model.PackageInformationDto

@Composable
fun UnblockedPackageListDialog(
    unblockedPackageList : List<PackageInformationDto> = listOf(),
    onDismissRequest : ()->Unit = {},
    onBlockBtnClick : (String, TargetType) -> Unit
){
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
            ,
            shape = RoundedCornerShape(16.dp),
        ){
            TitleBar(text = "Block new app", actionLeft = {
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Back"
                    )
                }
            })
            LazyColumn(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                items(unblockedPackageList){
                    info -> BlockingCard(
                    displayName = info.displayName,
                    packageNameOrUrl = info.packageName,
                    icon = info.icon,
                    onManage = onBlockBtnClick,
                    targetType = TargetType.APP,
                    btnText = "Block"
                )
                }
            }

            Row(Modifier.height(20.dp).fillMaxWidth()){}
        }
    }
}

