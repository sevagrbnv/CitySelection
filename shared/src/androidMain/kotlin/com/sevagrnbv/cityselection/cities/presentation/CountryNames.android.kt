package com.sevagrnbv.cityselection.cities.presentation

import java.util.Locale

internal actual fun platformCountryNames(): CountryNames = CountryNames { code ->
    try {
        Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.getDefault())
            .ifBlank { code }
    } catch (error: java.util.IllformedLocaleException) {
        code
    }
}
