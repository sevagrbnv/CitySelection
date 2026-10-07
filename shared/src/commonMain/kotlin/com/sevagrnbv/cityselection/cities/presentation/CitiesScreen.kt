package com.sevagrnbv.cityselection.cities.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cityselection.shared.generated.resources.*
import com.sevagrnbv.cityselection.core.network.NetworkError
import com.sevagrnbv.cityselection.theme.CitySelectionTheme
import com.sevagrnbv.cityselection.theme.Spacing
import com.sevagrnbv.cityselection.theme.IconSize
import com.sevagrnbv.cityselection.theme.CornerRadius
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun CitiesRoute(
    onCitySelected: (CityListItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CitiesViewModel = koinViewModel(),
) {
    val state by viewModel.collectAsState()

    CitiesScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onLoadNextPage = { viewModel.loadNextPage() },
        onRetry = { viewModel.retry() },
        onCitySelected = onCitySelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CitiesScreen(
    state: CitiesState,
    onQueryChanged: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onCitySelected: (CityListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val search = rememberTextFieldState(initialText = state.query)
    val queryCallback by rememberUpdatedState(onQueryChanged)
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(search) {
        snapshotFlow { search.text.toString() }
            .distinctUntilChanged()
            .collect { queryCallback(it) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.cities_title)) })
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                state = search,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                placeholder = { Text(stringResource(Res.string.cities_search_hint)) },
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                onKeyboardAction = { keyboard?.hide() },
                trailingIcon = {
                    if (search.text.isNotEmpty()) {
                        IconButton(onClick = { search.edit { replace(0, length, "") } }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = stringResource(Res.string.cities_clear_search)
                            )
                        }
                    } else Icon(painterResource(Res.drawable.ic_search), contentDescription = null)
                },
                shape = RoundedCornerShape(CornerRadius.medium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
            when (val content = state.content) {
                CitiesContent.Loading -> CitiesSkeleton()
                CitiesContent.Empty -> StatusContent(
                    title = stringResource(Res.string.cities_empty_title),
                    description = stringResource(Res.string.cities_empty_description),
                )

                is CitiesContent.Error -> StatusContent(
                    title = errorTitle(content.error),
                    description = stringResource(
                        if (content.error == NetworkError.Network)
                            Res.string.cities_network_description else Res.string.cities_error_description
                    ),
                    onRetry = onRetry,
                )

                is CitiesContent.Data -> CitiesList(
                    content,
                    state.nextPage,
                    onLoadNextPage,
                    onRetry,
                    onCitySelected,
                )
            }
        }
    }
}

@Composable
private fun CitiesList(
    content: CitiesContent.Data,
    nextPage: NextPageState,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onCitySelected: (CityListItem) -> Unit,
) {
    val listState = rememberLazyListState()
    val loadCallback by rememberUpdatedState(onLoadNextPage)
    LaunchedEffect(listState, content.items.size, content.hasNextPage, nextPage) {
        if (!content.hasNextPage || nextPage != NextPageState.Idle) return@LaunchedEffect
        snapshotFlow {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            layout.totalItemsCount > 0 && lastVisible >= layout.totalItemsCount - PREFETCH_DISTANCE
        }.distinctUntilChanged().filter { it }.collect { loadCallback() }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.medium)
    ) {
        items(content.items, key = { it.id }, contentType = { "city" }) { city ->
            ListItem(
                modifier = Modifier.clickable { onCitySelected(city) },
                headlineContent = {
                    Text(
                        stringResource(
                            Res.string.cities_item_label,
                            city.name,
                            city.countryName
                        )
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_location),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(IconSize.standard),
                    )
                },
            )
            HorizontalDivider(
                Modifier.padding(start = CITY_DIVIDER_START, end = Spacing.medium),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        when (nextPage) {
            NextPageState.Idle -> Unit
            NextPageState.Loading -> item(key = "loading") {
                val description = stringResource(Res.string.cities_loading_more)
                Box(
                    Modifier.fillMaxWidth().padding(Spacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        Modifier.size(PAGE_PROGRESS_SIZE).semantics { contentDescription = description },
                        strokeWidth = PAGE_PROGRESS_STROKE
                    )
                }
            }

            is NextPageState.Error -> item(key = "page_error") {
                Column(
                    Modifier.fillMaxWidth().padding(Spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(Res.string.cities_page_error),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onRetry) { Text(stringResource(Res.string.cities_retry)) }
                }
            }
        }
    }
}

@Composable
private fun CitiesSkeleton() {
    val description = stringResource(Res.string.cities_loading)
    Column(Modifier.fillMaxWidth().semantics { contentDescription = description }) {
        repeat(SKELETON_ROWS) { index ->
            Row(
                Modifier.fillMaxWidth().height(CITY_ROW_HEIGHT).padding(horizontal = Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_location),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(IconSize.standard),
                )
                Spacer(Modifier.width(SKELETON_ICON_GAP))
                Box(
                    Modifier.width(if (index % 2 == 0) SKELETON_SHORT_WIDTH else SKELETON_LONG_WIDTH).height(SKELETON_LINE_HEIGHT)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(CornerRadius.small)
                        )
                )
            }
        }
    }
}

@Composable
private fun StatusContent(title: String, description: String, onRetry: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(Spacing.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.compact))

        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (onRetry != null) {
            Spacer(Modifier.height(Spacing.large))
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(Res.string.cities_retry)) }
        }
    }
}

@Composable
private fun errorTitle(error: NetworkError): String = stringResource(
    when (error) {
        NetworkError.Network -> Res.string.cities_network_error
        NetworkError.Timeout -> Res.string.cities_timeout_error
        NetworkError.InvalidResponse -> Res.string.cities_response_error
        is NetworkError.Http -> Res.string.cities_server_error
    }
)

@Preview
@Composable
internal fun CitiesScreenPreview() {
    CitySelectionTheme {
        CitiesScreen(
            state = CitiesState(
                content = CitiesContent.Data(
                    listOf(
                        CityListItem(
                            id = 1,
                            name = "Moscow",
                            countryName = "Россия"
                        )
                    ), false
                )
            ),
            onQueryChanged = {}, onLoadNextPage = {}, onRetry = {}, onCitySelected = {},
        )
    }
}

// Геометрия строк и скелетона относится только к этому экрану.
private val CITY_ROW_HEIGHT = 56.dp
private val CITY_DIVIDER_START = 56.dp
private val SKELETON_ICON_GAP = 20.dp
private val SKELETON_SHORT_WIDTH = 150.dp
private val SKELETON_LONG_WIDTH = 190.dp
private val SKELETON_LINE_HEIGHT = 16.dp
private val PAGE_PROGRESS_SIZE = 24.dp
private val PAGE_PROGRESS_STROKE = 2.dp
private const val PREFETCH_DISTANCE = 5
private const val SKELETON_ROWS = 5
