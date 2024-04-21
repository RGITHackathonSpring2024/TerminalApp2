package ru.nosqd.rgit.terminalapp2

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import java.nio.charset.StandardCharsets

class NDefHelper(private val context: Context) {
    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    fun setupForegroundDispatch(activity: Activity) {
        val intent = Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type.", e)
            }
        })
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, filters, null)
    }

    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun readFromTag(tag: Tag): String? {
        return Ndef.get(tag)?.use { ndef ->
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            String(ndefMessage.records[0].payload)
        }
    }

    companion object {
        fun readTagData(tag: Tag): String? {
            val ndef = Ndef.get(tag)
            return ndef?.use { ndef ->
                ndef.connect()
                ndef.ndefMessage?.records?.get(0)?.payload?.let { String(it) }
            }
        }

        fun writeTagData(tag: Tag, data: String) {
            val ndef = Ndef.get(tag)
            val record = NdefRecord.createMime("rgit/rndpass", data.toByteArray(StandardCharsets.UTF_8))
            val message = NdefMessage(arrayOf(record))

            ndef?.use { ndef ->
                ndef.connect()
                ndef.writeNdefMessage(message)
            }
        }
    }
}
