package com.sevagrnbv.cityselection.cities.presentation

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.currentLocale

internal actual fun platformCountryNames(): CountryNames = CountryNames { code ->
    NSLocale.currentLocale.displayNameForKey(NSLocaleCountryCode, code)
        ?.takeIf { it.isNotBlank() } ?: code
}
