package com.example.lockedindungeon.activities

import android.Manifest
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.getSystemService
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.lockedindungeon.components.NavigationParent
import com.example.lockedindungeon.components.NavigationRoute
import com.example.lockedindungeon.components.ParentNavigationBar
import com.example.lockedindungeon.components.TitleBar
import com.example.lockedindungeon.feature.NfcWrapper
import com.example.lockedindungeon.services.AppBlockService
import com.example.lockedindungeon.ui.theme.LockedInDungeonTheme
import com.example.lockedindungeon.viewmodels.HomeViewModel
import com.example.lockedindungeon.viewmodels.NfcUnlockDialogViewmodel
import com.example.lockedindungeon.viewmodels.WriteTagViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity(
) : ComponentActivity() {


    private val homeViewModel: HomeViewModel by viewModels()
    private val writeTagViewModel: WriteTagViewModel by viewModels()
    private val nfcUnlockDialogViewmodel: NfcUnlockDialogViewmodel by viewModels()
    private lateinit var navController: NavHostController
    @Inject lateinit var nfcWrapper : NfcWrapper;

    companion object {
        val tag : String = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tag, "Log d is working")
        Log.i(tag, "Log i is working")
        Log.e(tag, "Log e is working")
        deployAssetToInternalStorage(this, "block-page.html")
        enableEdgeToEdge()
        if(!isAccessibilityEnabled()){
            redirectToSetting()
        }
        if(!hasUsageStatsPermission()){
            requestUsageStatsPermission()
        }


        setContent {

            LockedInDungeonTheme {
                navController = rememberNavController()

                var notificationDialogClosed by remember { mutableStateOf(false) }
                if(!isNotificationAllowed() && !notificationDialogClosed){
                    AskNotificationPermissionDialog({notificationDialogClosed = true}, {notificationDialogClosed = true})
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        ParentNavigationBar(navController)
                    }
                ) { innerPadding ->
                    NavigationParent(
                        navController,
                        Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcWrapper.enable()
    }

    override fun onPause() {
        super.onPause()
        nfcWrapper.disable()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d(tag, "New NFC intent received, event : ${intent.action}")
        if (NfcAdapter.ACTION_NDEF_DISCOVERED != intent.action) return

        // a card tapped while registering a new one gets written to, no matter the screen
        if (writeTagViewModel.onNdefIntent(intent)) return

        // otherwise it is the toggle-blocking flow, which only the home screen has
        val isOnSettings = navController.currentDestination
            ?.hasRoute(NavigationRoute.SettingsScreenRoute::class) == true
        if (!isOnSettings) {
//            klo bukan di settings tapi ada intent masuk, brarti dia di homescreen dan lagi mau toggle blocking
            nfcUnlockDialogViewmodel.onNdefIntent(intent)
            homeViewModel.onNdefIntent(intent)
        }
    }

//    fun scheduleSnooze(durationMinute : Int = 5){
//        val request : WorkRequest = OneTimeWorkRequestBuilder<SnoozeWorker>()
//            .setInitialDelay(Duration.ofMinutes(durationMinute.toLong()))
//            .build()
//
//        WorkManager.getInstance(this).enqueue(
//            request = request
//        )
//    }




    fun isAccessibilityEnabled() : Boolean {
        val serviceName = packageName + "/" + AppBlockService::class.java.canonicalName
        val accessibilityEnabled = try {
            Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            Log.e("MainActivity", "Error finding setting, default accessibility to not found: $e")
            0
        }

        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            Log.d(tag, "Setting value: $settingValue")
            Log.d(tag, "Looking for service: $serviceName")

            return settingValue.split(":").contains(serviceName)
        }

        return false
    }


    fun isNotificationAllowed() : Boolean{
        return getSystemService<NotificationManager>()?.areNotificationsEnabled() ?: false
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun redirectToSetting(){
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            // Flags ensure the settings page handles navigation correctly
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }


    fun deployAssetToInternalStorage(context: Context, assetFileName: String) {
        // 1. Create the target subdirectory inside internal storage (Context.getFilesDir())
        val internalImagesDir = File(context.filesDir, "block")
        if (!internalImagesDir.exists()) {
            internalImagesDir.mkdirs()
        }

        val targetFile = File(internalImagesDir, assetFileName)

        // Only copy if the file doesn't exist yet (or if you updated the asset)
        if (!targetFile.exists()) {
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    Log.d(tag, "Copying internal asset to file provider")
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LockedInDungeonTheme {
        Greeting("Android")
    }
}


@Composable
fun AskNotificationPermissionDialog(
    onGranted : () -> Unit,
    onDenied : () -> Unit
){
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted.invoke() else onDenied.invoke()
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
        ) {
            Column(
                Modifier.padding(24.dp),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Turn on notifications",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    "LockedInDungeon needs notifications to tell you when a snooze is over and blocking is turned back on",
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Enable Notifications")
                }
            }
        }
    }
}