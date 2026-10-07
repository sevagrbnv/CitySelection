package com.sevagrnbv.cityselection.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect fun platformEngineFactory(): HttpClientEngineFactory<*>

/** Клиент владеет движком, созданным фабрикой, и освобождает его при закрытии. */
internal fun createHttpClient(factory: HttpClientEngineFactory<*>): HttpClient =
    HttpClient(factory) { configureClient() }

/** Переданный напрямую движок остаётся во владении вызывающей стороны; используется в тестах. */
internal fun createHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) { configureClient() }

private fun HttpClientConfig<*>.configureClient() {
    expectSuccess = true
    // Перенаправления с разрешённого HTTP-домена API на другие адреса отключены.
    followRedirects = false
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}

private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val SOCKET_TIMEOUT_MILLIS = 15_000L
private const val REQUEST_TIMEOUT_MILLIS = 20_000L
