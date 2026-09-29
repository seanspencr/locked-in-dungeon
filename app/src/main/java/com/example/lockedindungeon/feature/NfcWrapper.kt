package com.example.lockedindungeon.feature

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import dagger.hilt.android.scopes.ActivityScoped
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
            addDataType("*/*")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("fail", e)
        }
    }

    private val techLists = arrayOf(
        arrayOf(Ndef::class.java.name),
    )


    private val intentFilters = arrayOf(ndefFilter)

    fun enable() {
        nfcAdapter?.enableForegroundDispatch(
            activity,
            pendingIntent,
            intentFilters,
            techLists
        )
    }

    fun disable() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

}