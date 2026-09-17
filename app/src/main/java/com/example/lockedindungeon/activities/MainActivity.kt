package com.example.lockedindungeon.activities

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.lockedindungeon.components.NavigationParent
import com.example.lockedindungeon.components.ParentNavigationBar
import com.example.lockedindungeon.feature.NfcWrapper
import com.example.lockedindungeon.services.AppBlockService
import com.example.lockedindungeon.ui.theme.LockedInDungeonTheme
import com.example.lockedindungeon.viewmodels.HomeViewModel
import com.example.lockedindungeon.viewmodels.NfcScanViewmodel
import com.example.lockedindungeon.viewmodels.WriteTagPasswordViewmodel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity(
) : ComponentActivity() {


    private val nfcScanViewModel: NfcScanViewmodel by viewModels()
    private val writeTagPasswordViewModel: WriteTagPasswordViewmodel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
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
                val navController = rememberNavController()

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

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            homeViewModel.onNdefIntent(intent)

//            if(homeViewModel.homeState.value.isNfcDialogOpen){
//                nfcScanViewModel.onTagDetected(intent)
//            }else if(homeViewModel.homeState.value.isWritePasswordDialogOpen){
//                writeTagPasswordViewModel.onTagDetected(intent)
//            }
        }
    }




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

    fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
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