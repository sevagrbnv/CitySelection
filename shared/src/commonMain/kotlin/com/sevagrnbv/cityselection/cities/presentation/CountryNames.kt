package com.sevagrnbv.cityselection.cities.presentation

/** Перевод кода страны относится к презентации; неизвестный код сохраняется. */
internal fun interface CountryNames {
    fun displayName(code: String): String
}

internal expect fun platformCountryNames(): CountryNames
