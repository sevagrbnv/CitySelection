package com.sevagrnbv.cityselection.cities.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cityselection.shared.generated.resources.*
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/** Общие поля отдельного экрана и модальной карточки города на карте. */
@Composable
internal fun CityDetailFields(name: String, countryName: String, population: Long?) {
    CityDetailField(stringResource(Res.string.city_details_name), name)
    CityDetailField(stringResource(Res.string.city_details_country), countryName)
    val populationText = population?.let {
        stringResource(Res.string.city_details_population_value, formatPopulation(it))
    } ?: stringResource(Res.string.city_details_unknown_population)
    CityDetailField(stringResource(Res.string.city_details_population), populationText)
}

@Composable
private fun CityDetailField(label: String, value: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = Spacing.fieldVertical),
        verticalArrangement = Arrangement.spacedBy(Spacing.tiny)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
