package ru.nosqd.rgit.terminalapp2.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.headers
import io.ktor.serialization.gson.gson

val client = HttpClient(Android) {
    install(ContentNegotiation) {
        gson()
    }
}