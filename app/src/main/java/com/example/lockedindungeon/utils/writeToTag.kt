package com.example.lockedindungeon.utils

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException

fun writeToTag(tag: Tag, text: String): Boolean {
    val message = NdefMessage(arrayOf(NdefRecord.createTextRecord("en", text)))
    val ndef = Ndef.get(tag)

    return if (ndef != null) {
        try {
            ndef.connect()
            if (!ndef.isWritable || ndef.maxSize < message.toByteArray().size) return false
            ndef.writeNdefMessage(message)
            true
        } catch (e: IOException) {
            false
        } finally {
            runCatching { ndef.close() }
        }
    } else {
        val formatable = NdefFormatable.get(tag)
        if (formatable != null) {
            try {
                formatable.connect()
                formatable.format(message)
                true
            } catch (e: IOException) {
                false
            } finally {
                runCatching { formatable.close() }
            }
        } else false
    }
}