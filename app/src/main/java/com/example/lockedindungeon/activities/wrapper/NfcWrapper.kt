package com.example.lockedindungeon.activities.wrapper

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.util.Log
import com.example.lockedindungeon.utils.parseTextRecord
import dagger.hilt.android.scopes.ActivityScoped
import java.nio.charset.Charset
import javax.inject.Inject


@ActivityScoped
class NfcWrapper @Inject constructor(
    private val activity: Activity
) {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            activity, 0,
            Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
    }
    val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
        try {
            addDataType("text/plain")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("fail", e)
        }
    }

    private val intentFilters = arrayOf(ndefFilter)

    fun enable() {
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
    }

    fun disable() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

}