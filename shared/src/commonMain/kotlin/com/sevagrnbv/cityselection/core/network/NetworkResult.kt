package com.sevagrnbv.cityselection.core.network

/** Результат сетевой операции с декодированной моделью приложения. */
sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>

    /** Исходная причина сохраняется для диагностики; UI использует тип [error]. */
    data class Failure(val error: NetworkError, val cause: Throwable) : NetworkResult<Nothing>
}
