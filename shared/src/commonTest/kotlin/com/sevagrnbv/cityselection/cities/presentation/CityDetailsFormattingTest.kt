package com.sevagrnbv.cityselection.cities.presentation

import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals

class CityDetailsFormattingTest {
    @Test
    fun populationPreservesZeroAndGroupsLargeValuesWithoutOverflow() {
        assertEquals("0", formatPopulation(0))
        assertEquals("999", formatPopulation(999))
        assertEquals("1\u00a0000", formatPopulation(1_000))
        assertEquals("12\u00a0655\u00a0000", formatPopulation(12_655_000))
        assertEquals("9\u00a0223\u00a0372\u00a0036\u00a0854\u00a0775\u00a0807", formatPopulation(Long.MAX_VALUE))
    }

    @Test
    fun searchTreatsUnicodeAndReservedCharactersAsOneQueryParameter() {
        val name = "São / Москва & q=Paris? #+"
        val url = Url(citySearchUrl(name))
        assertEquals("https", url.protocol.name)
        assertEquals("www.google.com", url.host)
        assertEquals("/search", url.encodedPath)
        assertEquals(name, url.parameters["q"])
        assertEquals(setOf("q"), url.parameters.names())
        assertEquals("", url.fragment)
    }
}
