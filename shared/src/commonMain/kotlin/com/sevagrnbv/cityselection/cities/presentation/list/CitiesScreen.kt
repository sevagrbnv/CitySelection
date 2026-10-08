package com.sevagrnbv.cityselection.cities.presentation.list

import androidx.compose.animation.core.*
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cityselection.shared.generated.resources.*
import com.sevagrnbv.cityselection.core.designsystem.component.PrimaryButton
import com.sevagrnbv.cityselection.core.designsystem.component.ScreenHeader
import com.sevagrnbv.cityselection.core.designsystem.component.SearchField
import com.sevagrnbv.cityselection.core.designsystem.theme.CitySelectionTheme
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentColors
import com.sevagrnbv.cityselection.core.designsystem.theme.ComponentSize
import com.sevagrnbv.cityselection.core.designsystem.theme.CornerRadius
import com.sevagrnbv.cityselection.core.designsystem.theme.IconSize
import com.sevagrnbv.cityselection.core.designsystem.theme.Spacing
import com.sevagrnbv.cityselection.core.designsystem.theme.TextColors
import com.sevagrnbv.cityselection.core.network.NetworkError
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
    onFullScreenErrorChanged: (Boolean) -> Unit = {},
    viewModel: CitiesViewModel = koinViewModel(),
) {
    val state by viewModel.collectAsState()
    val errorCallback by rememberUpdatedState(onFullScreenErrorChanged)

    DisposableEffect(state.content is CitiesContent.Error) {
        errorCallback(state.content is CitiesContent.Error)
        onDispose { errorCallback(false) }
    }

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
            if (state.content !is CitiesContent.Error) ScreenHeader(stringResource(Res.string.cities_title))
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.content !is CitiesContent.Error) SearchField(
                state = search,
                placeholder = stringResource(Res.string.cities_search_hint),
                onSearch = { keyboard?.hide() },
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            )
            when (val content = state.content) {
                CitiesContent.Loading -> CitiesSkeleton()
                CitiesContent.Empty -> StatusContent(
                    title = stringResource(Res.string.cities_empty_title),
                    description = stringResource(Res.string.cities_empty_description),
                )

                is CitiesContent.Error -> StatusContent(
                    showErrorIcon = true,
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
            Row(
                Modifier.fillMaxWidth().clickable { onCitySelected(city) }
                    .heightIn(min = ComponentSize.rowMinimumHeight)
                    .padding(horizontal = Spacing.medium, vertical = Spacing.compact),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.compact),
            ) {
                Icon(
                    painterResource(Res.drawable.ic_location), null,
                    tint = Color.Unspecified, modifier = Modifier.size(IconSize.standard)
                )
                Text(
                    stringResource(Res.string.cities_item_label, city.name, city.countryName),
                    style = MaterialTheme.typography.bodyLarge, color = TextColors.Primary,
                    modifier = Modifier.weight(1f)
                )
            }
            if (city.id != content.items.last().id) CityRowDivider()
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
                        Modifier.size(PAGE_PROGRESS_SIZE)
                            .semantics { contentDescription = description },
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
    val transition = rememberInfiniteTransition(label = "skeleton")
    val wave by transition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "wave"
    )

    Column(Modifier.fillMaxWidth().semantics { contentDescription = description }) {
        repeat(SKELETON_ROWS) { index ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = ComponentSize.rowMinimumHeight)
                    .padding(vertical = Spacing.compact).padding(horizontal = Spacing.medium),
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
                    Modifier.width(if (index % 2 == 0) SKELETON_SHORT_WIDTH else SKELETON_LONG_WIDTH)
                        .height(SKELETON_LINE_HEIGHT)
                        .background(
                            ComponentColors.Skeleton,
                            RoundedCornerShape(CornerRadius.medium)
                        ).drawWithContent {
                            drawContent()
                            val x = size.width * wave
                            drawRect(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        ComponentColors.Skeleton,
                                        Color.Transparent
                                    ),
                                    startX = x - size.width / 2, endX = x + size.width / 2
                                )
                            )
                        }
                )
            }
            if (index < SKELETON_ROWS - 1) CityRowDivider()
        }
    }
}

@Composable
private fun CityRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = Spacing.medium),
        color = ComponentColors.Divider,
    )
}

@Composable
private fun StatusContent(
    title: String,
    description: String,
    onRetry: (() -> Unit)? = null,
    showErrorIcon: Boolean = false,
) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = Spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showErrorIcon) {
                    Icon(
                        painterResource(Res.drawable.ic_error), null, tint = Color.Unspecified,
                        modifier = Modifier.size(IconSize.status)
                    )
                    Spacer(Modifier.height(Spacing.large))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextColors.Primary
                )
                Spacer(Modifier.height(Spacing.medium))
                Text(
                    description, style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center, color = TextColors.Secondary
                )
            }
        }
        if (onRetry != null) {
            PrimaryButton(
                stringResource(Res.string.cities_retry), onRetry,
                Modifier.fillMaxWidth().padding(Spacing.medium)
            )
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
private val SKELETON_ICON_GAP = 12.dp
private val SKELETON_SHORT_WIDTH = 144.dp
private val SKELETON_LONG_WIDTH = 180.dp
private val SKELETON_LINE_HEIGHT = 22.dp
private val PAGE_PROGRESS_SIZE = 24.dp
private val PAGE_PROGRESS_STROKE = 2.dp
private const val PREFETCH_DISTANCE = 5
private const val SKELETON_ROWS = 5
