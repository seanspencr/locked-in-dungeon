package com.example.lockedindungeon.activities

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lockedindungeon.ui.theme.LockedInDungeonTheme
import com.example.lockedindungeon.utils.parseTextRecord
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset


class MainActivity : ComponentActivity() {
    companion object {
        val tag : String = "MainActivity"
    }

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var intentFiltersArray: Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
//        redirectToSetting()

        deployAssetToInternalStorage(this, "block-page.html")
        setupNdefListener()

//        val intent = Intent(this, BlockActivity::class.java)
//        startActivity(intent)

        setContent {
            LockedInDungeonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Command the NFC hardware to route events directly here while foregrounded
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null)
    }

    override fun onPause() {
        super.onPause()
        // Critical: Release the hardware hook so other system components can run normally
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            // Your custom logic to parse NDEF messages goes here!
            Log.d(tag, "NFC Tag Discovered successfully!")
            onNdefReceived(intent)
        }
    }

    fun onNdefReceived(intent : Intent){
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
            val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
            messages.forEach { it -> logNfc(it) }
        }
    }

    fun logNfc(message: NdefMessage) {
        val parsedRecords = message.records.map { record ->
            when (record.tnf) {
                NdefRecord.TNF_WELL_KNOWN -> {
                    if (record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                        parseTextRecord(record)
                    } else {
                        "Payload Type: RTD_${String(record.type)}"
                    }
                }
                NdefRecord.TNF_ABSOLUTE_URI -> {
                    String(record.payload, Charset.forName("UTF-8"))
                }
                else -> "Unsupported Record Type (TNF: ${record.tnf})"
            }
        }

        // Using joinToString since fastJoinToString is typically a custom or library extension
        Log.d("NDEF_MESSAGE", parsedRecords.joinToString(separator = " | "))
    }


    fun redirectToSetting(){
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            // Flags ensure the settings page handles navigation correctly
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun setupNdefListener(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val flags = PendingIntent.FLAG_MUTABLE

//        flag : klo activtiy ini uda di atas, jangan spawn new activity
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

//        pendingIntent = kasi tau ke android, kalo kedetect NFC, jalanin intent ini
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }

        intentFiltersArray = arrayOf(ndefFilter)
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