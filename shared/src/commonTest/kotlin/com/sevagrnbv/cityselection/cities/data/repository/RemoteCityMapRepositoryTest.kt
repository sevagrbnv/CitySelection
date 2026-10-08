package com.sevagrnbv.cityselection.cities.data.repository

import com.sevagrnbv.cityselection.cities.data.remote.CitiesApi
import com.sevagrnbv.cityselection.cities.domain.model.City
import com.sevagrnbv.cityselection.core.network.NetworkError
import com.sevagrnbv.cityselection.core.network.NetworkResult
import com.sevagrnbv.cityselection.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest

class RemoteCityMapRepositoryTest {
    @Test
    fun sendsViewportAndMapsSeparateResponseEnvelope() = runTest {
        withRepository({ request ->
            assertEquals("/api/cities/map", request.url.encodedPath)
            assertEquals("55.75", request.url.parameters["centerLat"])
            assertEquals("37.62", request.url.parameters["centerLng"])
            assertEquals("12000", request.url.parameters["radius"])
            assertNull(request.url.parameters["page"])
            respond(
                """{"count":1,"items":[{"id":1,"name":"Moscow","country":"RU","lat":55.75,"lon":37.62,"pop":123}],"future":true}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }) { repository ->
            val result = repository.getCitiesAround(55.75, 37.62, 12000)
            val cities = assertIs<NetworkResult.Success<List<City>>>(result).value
            assertEquals(City(1, "Moscow", "RU", 55.75, 37.62, 123), cities.single())
        }
    }

    @Test
    fun malformedEnvelopeIsTypedFailure() = runTest {
        withRepository({
            respond("""{"count":1}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }) { repository ->
            val result = repository.getCitiesAround(0.0, 0.0, 1)
            assertEquals(NetworkError.InvalidResponse, assertIs<NetworkResult.Failure>(result).error)
        }
    }

    @Test
    fun cancellationPropagates() = runTest {
        withRepository({ throw CancellationException("cancelled map request") }) { repository ->
            assertFailsWith<CancellationException> { repository.getCitiesAround(0.0, 0.0, 1) }
        }
    }

    private suspend fun withRepository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
        test: suspend (RemoteCityMapRepository) -> Unit,
    ) {
        val engine = MockEngine(handler)
        val client = createHttpClient(engine)
        try {
            test(RemoteCityMapRepository(CitiesApi(client, "http://example.test/")))
        } finally {
            client.close()
            engine.close()
        }
    }
}
