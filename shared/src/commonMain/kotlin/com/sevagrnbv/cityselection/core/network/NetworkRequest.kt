package com.sevagrnbv.cityselection.core.network

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/** Общая обработка ожидаемых ошибок REST; отмена и ошибки программирования пробрасываются. */
internal suspend fun <T> networkRequest(block: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    NetworkResult.Failure(error.toNetworkError(), error)
}

/** Классифицирует только ожидаемые ошибки; неизвестное исключение пробрасывается вызывающему коду. */
private fun Exception.toNetworkError(): NetworkError = when (this) {
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> NetworkError.Timeout

    is ResponseException -> NetworkError.Http(response.status.value)
    is IOException -> NetworkError.Network

    is SerializationException,
    is ContentConvertException,
    is NoTransformationFoundException -> NetworkError.InvalidResponse

    else -> throw this
}
