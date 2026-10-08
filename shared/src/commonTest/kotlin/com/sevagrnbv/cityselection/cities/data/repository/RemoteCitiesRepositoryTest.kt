package com.sevagrnbv.cityselection.cities.data.repository

import com.sevagrnbv.cityselection.cities.data.mapper.toDomain
import com.sevagrnbv.cityselection.cities.data.remote.CitiesApi
import com.sevagrnbv.cityselection.cities.data.remote.CityDto
import com.sevagrnbv.cityselection.cities.domain.model.CitiesPage
import com.sevagrnbv.cityselection.cities.domain.model.City
import com.sevagrnbv.cityselection.core.network.NetworkError
import com.sevagrnbv.cityselection.core.network.NetworkResult
import com.sevagrnbv.cityselection.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

class RemoteCitiesRepositoryTest {
    @Test
    fun mapsResponseAndEncodesSearchParameters() = runTest {
        withRepository({ request ->
            assertEquals("/api/cities", request.url.encodedPath)
            assertEquals("São & Москва", request.url.parameters["query"])
            assertEquals("2", request.url.parameters["page"])
            assertEquals("1", request.url.parameters["limit"])
            respondJson("""{"items":[$city],"page":2,"limit":1,"total":3,"futureField":true}""")
        }) { repository ->
            val page = assertIs<NetworkResult.Success<CitiesPage>>(repository.getCities("  São & Москва  ", 2, 1)).value
            assertEquals(524901L, page.items.single().id)
            assertEquals("Moscow", page.items.single().name)
            assertEquals("RU", page.items.single().countryCode)
            assertEquals(10381222L, page.items.single().population)
            assertEquals(55.75204, page.items.single().latitude)
            assertEquals(37.61781, page.items.single().longitude)
            assertTrue(page.hasNextPage)
        }
    }

    @Test
    fun acceptsMissingOrNullOptionalMetadata() = runTest {
        val items = """{"id":1,"name":"First","country":"RU"},{"id":2,"name":"Second","country":"US","lat":null,"lon":null,"pop":null}"""
        withRepository({ respondJson("""{"items":[$items],"page":1,"limit":20,"total":2}""") }) {
            val page = assertIs<NetworkResult.Success<CitiesPage>>(it.getCities()).value
            assertTrue(page.items.all { city -> city.population == null && city.latitude == null && city.longitude == null })
            assertFalse(page.hasNextPage)
        }
    }

    @Test
    fun emptySearchResultIsSuccess() = runTest {
        withRepository({ respondJson("""{"items":[],"page":1,"limit":20,"total":0}""") }) {
            val page = assertIs<NetworkResult.Success<CitiesPage>>(it.getCities("unknown")).value
            assertTrue(page.items.isEmpty())
            assertFalse(page.hasNextPage)
        }
    }

    @Test
    fun partialFinalPageHasNoNextPageAndCalculationDoesNotOverflow() {
        assertFalse(CitiesPage(emptyList(), 1, 30, 0).hasNextPage)
        val item = CityDto(1, "City", "RU").toDomain()
        assertFalse(CitiesPage(listOf(item), 2, 30, 31).hasNextPage)
        assertFalse(CitiesPage(listOf(item), Int.MAX_VALUE, 30, Int.MAX_VALUE.toLong()).hasNextPage)
    }

    @Test
    fun preservesHttpStatusForClientAndServerErrors() = runTest {
        for (status in listOf(400, 404, 429, 500, 503)) {
            withRepository({ respondJson("{}", HttpStatusCode.fromValue(status)) }) {
                assertFailure(NetworkError.Http(status), it.getCities())
            }
        }
    }

    @Test
    fun redirectsAreNotFollowed() = runTest {
        withRepository({
            respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "http://other.example/cities"))
        }) {
            assertFailure(NetworkError.Http(302), it.getCities())
        }
    }

    @Test
    fun unexpectedContentTypeIsInvalidResponse() = runTest {
        withRepository({
            respond("<html>proxy error</html>", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/html"))
        }) {
            assertFailure(NetworkError.InvalidResponse, it.getCities())
        }
    }

    @Test
    fun rejectsMalformedOrIncompleteResponses() = runTest {
        for (body in listOf("not json", "{}", """{"items":[{"name":"Missing ID"}],"page":1,"limit":20,"total":1}""")) {
            withRepository({ respondJson(body) }) {
                assertFailure(NetworkError.InvalidResponse, it.getCities())
            }
        }
    }

    @Test
    fun mapsTransportFailure() = runTest {
        withRepository({ throw IOException("connection unavailable") }) {
            assertFailure(NetworkError.Network, it.getCities())
        }
    }

    @Test
    fun mapsRequestTimeout() = runTest {
        withRepository({ request -> throw HttpRequestTimeoutException(request) }) {
            assertFailure(NetworkError.Timeout, it.getCities())
        }
    }

    @Test
    fun cancellationIsNeverReturnedAsFailure() = runTest {
        val cancellation = CancellationException("search replaced")
        withRepository({ throw cancellation }) {
            assertFailsWith<CancellationException> { it.getCities() }
        }
    }

    @Test
    fun programmingErrorsAreNotDisguisedAsNetworkErrors() = runTest {
        val failure = IllegalStateException("unexpected bug")
        withRepository({ throw failure }) {
            assertEquals(failure.message, assertFailsWith<IllegalStateException> { it.getCities() }.message)
        }
    }

    private fun assertFailure(error: NetworkError, result: NetworkResult<*>): NetworkResult.Failure {
        val failure = assertIs<NetworkResult.Failure>(result)
        assertEquals(error, failure.error)
        return failure
    }

    private suspend fun withRepository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
        test: suspend (RemoteCitiesRepository) -> Unit,
    ) {
        val engine = MockEngine(handler)
        val client = createHttpClient(engine)
        try {
            test(RemoteCitiesRepository(CitiesApi(client, "http://test.example")))
        } finally {
            client.close()
            engine.close()
        }
    }

    private fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private val city = """{"id":524901,"name":"Moscow","country":"RU","lat":55.75204,"lon":37.61781,"pop":10381222}"""
}
