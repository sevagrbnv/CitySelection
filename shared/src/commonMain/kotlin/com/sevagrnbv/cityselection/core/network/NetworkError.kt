package com.sevagrnbv.cityselection.core.network

/** Общие ошибки транспорта и ответов сетевых операций. */
sealed interface NetworkError {
    /** Ошибка транспорта сама по себе не доказывает отсутствие интернета на устройстве. */
    data object Network : NetworkError
    data object Timeout : NetworkError
    data class Http(val statusCode: Int) : NetworkError
    data object InvalidResponse : NetworkError
}
