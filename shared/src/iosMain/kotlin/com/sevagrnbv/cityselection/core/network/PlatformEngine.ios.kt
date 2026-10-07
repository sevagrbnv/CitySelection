package com.sevagrnbv.cityselection.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun platformEngineFactory(): HttpClientEngineFactory<*> = Darwin
