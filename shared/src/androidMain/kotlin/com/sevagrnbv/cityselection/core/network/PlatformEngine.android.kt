package com.sevagrnbv.cityselection.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun platformEngineFactory(): HttpClientEngineFactory<*> = OkHttp
