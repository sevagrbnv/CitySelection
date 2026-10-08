package com.sevagrnbv.cityselection.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest

class HttpClientFactoryTest {
    @Test
    fun clientClosesItsFactoryCreatedEngine() = runTest {
        val engine = MockEngine { error("No request expected") }
        val factory = object : HttpClientEngineFactory<MockEngineConfig> {
            override fun create(block: MockEngineConfig.() -> Unit): HttpClientEngine = engine
        }
        val client = createHttpClient(factory)
        try {
            client.close()
            client.coroutineContext[Job]!!.join()
            engine.coroutineContext[Job]!!.join()
            assertTrue(engine.coroutineContext[Job]!!.isCompleted)
        } finally {
            client.close()
            engine.close()
        }
    }
}
