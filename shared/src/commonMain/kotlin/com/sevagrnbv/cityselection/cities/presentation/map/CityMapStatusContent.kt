package com.sevagrnbv.cityselection.cities.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cityselection.shared.generated.resources.*
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/** Состояние подложки независимо от загрузки городов из REST API. */
@Composable
internal fun MapLoadStatusContent(status: MapLoadStatus, onRetry: () -> Unit) {
    when (status) {
        MapLoadStatus.Loading -> MapLoadingMessage(stringResource(Res.string.map_loading))
        MapLoadStatus.Error -> MapRetryMessage(stringResource(Res.string.map_load_error), onRetry)
        MapLoadStatus.Ready -> Unit
    }
}

/** Во время запроса показываем загрузку; после завершения — ошибку или пустую выборку. */
@Composable
internal fun CitiesLoadStatusContent(state: CityMapState, onRetry: () -> Unit) {
    when {
        state.loading -> MapLoadingMessage(stringResource(Res.string.cities_loading))
        state.error != null -> MapRetryMessage(stringResource(Res.string.map_cities_error), onRetry)
        state.loaded && state.cities.isEmpty() -> Surface(shape = MaterialTheme.shapes.medium) {
            Text(stringResource(Res.string.map_empty), Modifier.padding(Spacing.compact))
        }
        else -> Unit
    }
}

@Composable
private fun MapLoadingMessage(message: String) {
    Surface(shape = MaterialTheme.shapes.medium, shadowElevation = Spacing.extraSmall) {
        Row(Modifier.padding(Spacing.compact), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            CircularProgressIndicator(Modifier.size(Spacing.large))
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MapRetryMessage(message: String, onRetry: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, shadowElevation = Spacing.extraSmall) {
        Column(Modifier.padding(Spacing.compact)) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.cities_retry)) }
        }
    }
}
