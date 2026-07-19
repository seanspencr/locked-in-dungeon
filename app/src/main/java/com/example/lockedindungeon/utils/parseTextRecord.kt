package com.example.lockedindungeon.utils

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import java.nio.charset.Charset


fun parseNdefIntent(intent: Intent): List<List<String>?>? {
    if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED) return null
    val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

    if (rawMsgs != null) {
        val contents = rawMsgs.map {
            getStringContent(it as NdefMessage)
        }.toList()
        return contents
    }

    return  null

}

private fun getStringContent(message: NdefMessage): List<String> {
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

    return parsedRecords

}
fun parseTextRecord(record: NdefRecord): String {
    val payload = record.payload
    if (payload.isEmpty()) return ""

    // The status byte (first byte) contains encoding and language length info
    val statusByte = payload[0].toInt()

    // Bit 7 determines text encoding (0 = UTF-8, 1 = UTF-16)
    val isUtf16 = (statusByte and 0x80) != 0
    val textEncoding = if (isUtf16) Charsets.UTF_16 else Charsets.UTF_8

    // Bits 5-0 contain the length of the language code string (e.g., "en")
    val languageCodeLength = statusByte and 0x1F

    // The actual text starts right after the status byte and the language code
    val textOffset = 1 + languageCodeLength
    val textLength = payload.size - textOffset

    if (textLength <= 0) return ""

    return String(payload, textOffset, textLength, textEncoding)
}