package com.sevagrnbv.cityselection.cities.presentation.common

import java.util.IllformedLocaleException
import java.util.Locale

internal actual fun platformCountryNames(): CountryNames = CountryNames { code ->
    try {
        Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.getDefault())
            .ifBlank { code }
    } catch (_: IllformedLocaleException) {
        code
    }
}
