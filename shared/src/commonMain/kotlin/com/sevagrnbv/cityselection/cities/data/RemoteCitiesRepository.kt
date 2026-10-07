package com.sevagrnbv.cityselection.cities.data

import com.sevagrnbv.cityselection.core.network.NetworkError
import com.sevagrnbv.cityselection.cities.domain.CitiesRepository
import com.sevagrnbv.cityselection.cities.domain.CitiesPage
import com.sevagrnbv.cityselection.core.network.NetworkResult
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

internal class RemoteCitiesRepository(private val api: CitiesApi) : CitiesRepository {
    override suspend fun getCities(query: String, page: Int, limit: Int): NetworkResult<CitiesPage> {
        require(page >= 1) { "page must be one-based" }
        require(limit > 0) { "limit must be positive" }
        return try {
            NetworkResult.Success(api.getCities(query.trim(), page, limit).toDomain())
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpRequestTimeoutException) {
            NetworkResult.Failure(NetworkError.Timeout, error)
        } catch (error: ConnectTimeoutException) {
            NetworkResult.Failure(NetworkError.Timeout, error)
        } catch (error: SocketTimeoutException) {
            NetworkResult.Failure(NetworkError.Timeout, error)
        } catch (error: ResponseException) {
            NetworkResult.Failure(NetworkError.Http(error.response.status.value), error)
        } catch (error: IOException) {
            NetworkResult.Failure(NetworkError.Network, error)
        } catch (error: SerializationException) {
            NetworkResult.Failure(NetworkError.InvalidResponse, error)
        } catch (error: ContentConvertException) {
            NetworkResult.Failure(NetworkError.InvalidResponse, error)
        } catch (error: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.InvalidResponse, error)
        }
    }
}
