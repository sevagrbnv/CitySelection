package com.sevagrnbv.cityselection.cities.presentation.common

import io.ktor.http.URLBuilder

/** Группирует число неразрывными пробелами для русского интерфейса. */
internal fun formatPopulation(population: Long): String {
    val value = population.toString()
    val digits = value.removePrefix("-")
    val grouped = digits.reversed().chunked(3).joinToString("\u00a0").reversed()
    return if (value.startsWith("-")) "-$grouped" else grouped
}

/** Имя кодируется как один параметр, включая пробелы и специальные символы. */
internal fun citySearchUrl(name: String): String = URLBuilder("https://www.google.com/search")
    .apply { parameters.append("q", name) }.buildString()
