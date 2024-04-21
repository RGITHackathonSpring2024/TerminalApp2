package ru.nosqd.rgit.terminalapp2

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import ru.nosqd.rgit.terminalapp2.api.API_SECRET
import ru.nosqd.rgit.terminalapp2.api.API_URL
import ru.nosqd.rgit.terminalapp2.api.INSTITUTION_ID
import ru.nosqd.rgit.terminalapp2.api.ProcessCardRequest
import ru.nosqd.rgit.terminalapp2.api.ProcessCardResponse
import ru.nosqd.rgit.terminalapp2.api.ProcessTransactionRequest
import ru.nosqd.rgit.terminalapp2.api.ProcessTransactionResponse
import ru.nosqd.rgit.terminalapp2.api.client
import ru.nosqd.rgit.terminalapp2.ui.theme.TerminalApp2Theme

class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContent {
            NFCReaderApp()
        }
    }

    @Composable
    fun NFCReaderApp() {
        Surface {
            Column {
               Text("Как то вы приложите карточку с неё будет списано 100 рублей.")
            }
        }
    }

    private fun promptNfcReading() {
        Toast.makeText(this, "Place NFC tag near your phone", Toast.LENGTH_LONG).show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { tag ->
                handleTag(tag)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { tag ->
                handleTag(tag)
            }
        }
    }

    private fun handleTag(tag: Tag) {
        val data = NDefHelper.readTagData(tag)
        data?.let {
            if (!it.startsWith("pass:")) {
                Toast.makeText(this, "Error: Not a valid card", Toast.LENGTH_LONG).show()
                return
            }

            val cardToken = it.removePrefix("pass:")
            updateCard(tag, cardToken)
        }
    }

    private fun updateCard(tag: Tag, oldToken: String) {
        lifecycleScope.launch {
            try {

                val g = Gson()
                val response = client.post(API_URL + "/internalapis/update-card") {
                    contentType(ContentType.Application.Json)
                    setBody(ProcessCardRequest(oldToken))
                    header(HttpHeaders.Authorization, API_SECRET)
                }
                val responseBody = response.bodyAsText()
                val data = g.fromJson(responseBody, ProcessCardResponse::class.java)
                Log.i("helo", data.cardId)
                Log.i("helo", oldToken)
                Log.i("helo", data.newToken)
                if (data.cardId.isNotEmpty()) {
                    val newData = "pass:${data.newToken}"
                    NDefHelper.writeTagData(tag, newData)
                    Log.i("test", "Card with Id ${data.cardId} changed token from $oldToken to ${data.newToken}")
                    val transResponse = client.post("$API_URL/internalapis/process-transaction") {
                        contentType(ContentType.Application.Json)
                        setBody(ProcessTransactionRequest(data.cardId, INSTITUTION_ID, 100))
                        header(HttpHeaders.Authorization, API_SECRET)
                    }
                    val transResponseBody = transResponse.bodyAsText()
                    val transData = g.fromJson(transResponseBody, ProcessTransactionResponse::class.java)
                    Log.i("TEST", transResponseBody)
                    Log.i("TEST", transResponse.status.value.toString())
                    if (transResponse.status.value != 200) {
                        Toast.makeText(this@MainActivity, "ОШИБКА: ${transData.message}", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(this@MainActivity, "УСПЕХ: ${transData.transactionId}", Toast.LENGTH_LONG).show()

                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error updating card", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("NFCUpdate", "Failed to update card: ${e.localizedMessage}")
            }
        }
    }
}