package com.sevagrnbv.cityselection.cities.presentation.map

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sevagrnbv.cityselection.core.designsystem.resource.AndroidFonts
import com.sevagrnbv.cityselection.core.designsystem.theme.CityPinStyle
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

@Composable
internal actual fun NativeCityMap(
    initialCamera: MapCamera,
    cities: List<CityMapMarker>,
    selectedId: Long?,
    onViewportChanged: (MapViewport) -> Unit,
    onCitySelected: (Long) -> Unit,
    onMapTapped: () -> Unit,
    onLoadStatus: (MapLoadStatus) -> Unit,
    onControlsChanged: (MapControls?) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val viewportCallback by rememberUpdatedState(onViewportChanged)
    val selectionCallback by rememberUpdatedState(onCitySelected)
    val mapTapCallback by rememberUpdatedState(onMapTapped)
    val controlsCallback by rememberUpdatedState(onControlsChanged)
    val statusCallback by rememberUpdatedState(onLoadStatus)
    var holder by remember { mutableStateOf<AndroidCityMap?>(null) }

    LaunchedEffect(holder) {
        holder?.setPinFont(AndroidFonts.robotoSemiBold(context))
    }

    DisposableEffect(holder, lifecycle) {
        val current = holder
        current?.bindLifecycle(lifecycle)
        controlsCallback(current?.let { MapControls(it::zoomBy) })
        onDispose {
            current?.unbindLifecycle()
            controlsCallback(null)
        }
    }
    AndroidView(
        factory = { viewContext ->
            AndroidCityMap(
                view = MapView(viewContext, MapLibreMapOptions.createFromAttributes(viewContext).textureMode(true)),
                initialCamera = initialCamera,
                onViewport = { viewportCallback(it) },
                onSelection = { selectionCallback(it) },
                onMapTapped = { mapTapCallback() },
                onStatus = { statusCallback(it) },
            ).also { holder = it }.view
        },
        modifier = modifier,
        onRelease = { view ->
            holder?.takeIf { it.view === view }?.dispose()
            holder = null
        },
        update = { holder?.update(cities, selectedId) },
    )
}

/** SDK и его слушатели живут ровно столько, сколько нативное представление в Compose. */
private class AndroidCityMap(
    val view: MapView,
    initialCamera: MapCamera,
    private val onViewport: (MapViewport) -> Unit,
    private val onSelection: (Long) -> Unit,
    private val onMapTapped: () -> Unit,
    private val onStatus: (MapLoadStatus) -> Unit,
) {
    private var pinFont = Typeface.create("sans-serif", Typeface.BOLD)
    fun setPinFont(font: Typeface) {
        pinFont = font
        pinImages.clear()
        updatePinImages()
    }
    private var map: MapLibreMap? = null
    private var source: GeoJsonSource? = null
    private var style: Style? = null
    private val pinImages = mutableMapOf<Long, Pair<String, Boolean>>()
    private var cities: List<CityMapMarker> = emptyList()
    private var selectedId: Long? = null
    private var started = false
    private var resumed = false
    private var disposed = false
    private var lifecycleCleanup: (() -> Unit)? = null

    init {
        view.onCreate(null)
        view.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) emitViewport()
        }
        view.addOnDidFailLoadingMapListener { if (!disposed) onStatus(MapLoadStatus.Error) }
        view.getMapAsync { sdk ->
            if (disposed) return@getMapAsync
            map = sdk
            sdk.setMinZoomPreference(CityMapConfig.minZoom)
            sdk.setMaxZoomPreference(CityMapConfig.maxZoom)
            sdk.uiSettings.apply {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isAttributionEnabled = false
                isLogoEnabled = false
            }
            sdk.cameraPosition = CameraPosition.Builder()
                .target(LatLng(initialCamera.latitude, initialCamera.longitude)).zoom(initialCamera.zoom).build()
            sdk.addOnCameraIdleListener { emitViewport() }
            sdk.addOnMapClickListener { coordinate ->
                val point = sdk.projection.toScreenLocation(coordinate)
                val tolerance = CityPinStyle.hitTestRadius * view.resources.displayMetrics.density
                val hit = sdk.queryRenderedFeatures(
                    RectF(point.x - tolerance, point.y - tolerance, point.x + tolerance, point.y + tolerance),
                    SELECTED_LABELS, LABELS,
                ).firstOrNull()
                val id = hit?.getStringProperty("cityId")?.toLongOrNull()
                if (id != null) onSelection(id) else onMapTapped()
                id != null
            }
            sdk.setStyle(Style.Builder().fromUri(CityMapConfig.styleUrl)) { style ->
                if (disposed) return@setStyle
                val data = GeoJsonSource(SOURCE, features())
                style.addSource(data)
                source = data
                this.style = style
                updatePinImages()
                fun pinLayer(id: String, selected: Boolean) = SymbolLayer(id, SOURCE).withFilter(
                    eq(get("selected"), literal(selected)),
                ).withProperties(
                    iconImage(get("pinImage")), iconAnchor("bottom"), iconOffset(arrayOf(0f, CityPinStyle.iconOffsetY)),
                    iconAllowOverlap(selected), iconIgnorePlacement(selected),
                )
                style.addLayer(pinLayer(LABELS, false))
                style.addLayer(pinLayer(SELECTED_LABELS, true))
                onStatus(MapLoadStatus.Ready)
                view.post { if (!disposed) emitViewport() }
            }
        }
    }

    fun update(value: List<CityMapMarker>, selected: Long?) {
        if (cities == value && selectedId == selected) return
        cities = value
        selectedId = selected
        updatePinImages()
        source?.setGeoJson(features())
    }

    private fun updatePinImages() {
        val style = style ?: return
        val ids = cities.mapTo(mutableSetOf()) { it.id }
        for (id in pinImages.keys.toList()) if (id !in ids) {
            style.removeImage("city-pin-$id")
            pinImages.remove(id)
        }
        for (city in cities) {
            val value = city.name to (city.id == selectedId)
            if (pinImages[city.id] == value) continue
            val image = cityPinBitmap(city.name, value.second, pinFont, view.resources.displayMetrics.density)
            style.addImage("city-pin-${city.id}", image)
            pinImages[city.id] = value
        }
    }

    private fun features(): FeatureCollection = FeatureCollection.fromFeatures(cities.map { city ->
        Feature.fromGeometry(Point.fromLngLat(city.longitude, city.latitude)).apply {
            addStringProperty("cityId", city.id.toString())
            addStringProperty("pinImage", "city-pin-${city.id}")
            addBooleanProperty("selected", city.id == selectedId)
        }
    })

    private fun emitViewport() {
        if (disposed || view.width == 0 || view.height == 0 || source == null) return
        val sdk = map ?: return
        val position = sdk.cameraPosition
        val target = position.target ?: return
        val camera = MapCamera(target.latitude, target.longitude, position.zoom)
        val region = sdk.projection.visibleRegion
        val corners = listOfNotNull(region.farLeft, region.farRight, region.nearLeft, region.nearRight)
        if (corners.size != 4) return
        onViewport(MapViewport(camera, viewportRadius(camera, corners.map { it.latitude to it.longitude })))
    }

    fun zoomBy(delta: Double) {
        val sdk = map ?: return
        sdk.animateCamera(CameraUpdateFactory.zoomTo((sdk.cameraPosition.zoom + delta).coerceIn(CityMapConfig.minZoom, CityMapConfig.maxZoom)))
    }

    fun bindLifecycle(lifecycle: Lifecycle) {
        unbindLifecycle()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_RESUME -> resume()
                Lifecycle.Event.ON_PAUSE -> pause()
                Lifecycle.Event.ON_STOP -> stop()
                else -> Unit
            }
        }
        val memory = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() = view.onLowMemory()
        }
        val application = view.context.applicationContext
        lifecycleCleanup = {
            lifecycle.removeObserver(observer)
            application.unregisterComponentCallbacks(memory)
            stop()
        }
        application.registerComponentCallbacks(memory)
        lifecycle.addObserver(observer)
    }

    fun unbindLifecycle() {
        lifecycleCleanup?.invoke()
        lifecycleCleanup = null
    }

    fun start() { if (!started && !disposed) { view.onStart(); started = true } }
    fun resume() { start(); if (!resumed && !disposed) { view.onResume(); resumed = true } }
    fun pause() { if (resumed) { view.onPause(); resumed = false } }
    fun stop() { pause(); if (started) { view.onStop(); started = false } }
    fun dispose() {
        if (disposed) return
        unbindLifecycle()
        stop()
        disposed = true
        source = null
        style = null
        pinImages.clear()
        map = null
        view.onDestroy()
    }

    private companion object {
        const val SOURCE = "city-selection-source"
        const val SELECTED_LABELS = "city-selection-selected-labels"
        const val LABELS = "city-selection-labels"
    }
}

/** Плашка и хвостик — единый контур, без внутренних швов и вспомогательных фигур. */
private fun cityPinBitmap(name: String, selected: Boolean, font: Typeface, density: Float): Bitmap {
    val pin = CityPinStyle
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pin.foregroundArgb
        textSize = pin.fontSize
        typeface = font
    }
    val width = kotlin.math.ceil(text.measureText(name) + pin.horizontalPadding * 2).coerceAtLeast(pin.minimumWidth)
    val imageHeight = pin.bodyHeight + pin.tailHeight + pin.imagePadding * 2
    val image = Bitmap.createBitmap(kotlin.math.ceil((width + pin.imagePadding * 2) * density).toInt(),
        kotlin.math.ceil(imageHeight * density).toInt(), Bitmap.Config.ARGB_8888)
    image.density = (density * 160).toInt()
    val canvas = Canvas(image)
    canvas.scale(density, density)
    canvas.translate(pin.imagePadding, pin.imagePadding)
    val center = width / 2
    val capRadius = pin.bodyHeight / 2
    val top = pin.strokeWidth / 2
    val bottom = pin.bodyHeight - top
    val tailBottom = bottom + pin.tailHeight
    val curve = pin.capsuleControlOffset
    val path = Path().apply {
        moveTo(capRadius, top)
        lineTo(width - capRadius, top)
        cubicTo(width + curve, top, width + curve, bottom, width - capRadius, bottom)
        lineTo(center + pin.tailHalfWidth, bottom)
        cubicTo(center + pin.tailControlOffset, bottom, center + pin.tailControlOffset, tailBottom, center, tailBottom)
        cubicTo(center - pin.tailControlOffset, tailBottom, center - pin.tailControlOffset, bottom, center - pin.tailHalfWidth, bottom)
        lineTo(capRadius, bottom)
        cubicTo(-curve, bottom, -curve, top, capRadius, top)
        close()
    }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (selected) pin.selectedBackgroundArgb else pin.backgroundArgb
    }
    paint.setShadowLayer(pin.shadowRadius, 0f, pin.shadowOffsetY, pin.shadowArgb)
    canvas.drawPath(path, paint)
    paint.clearShadowLayer()
    paint.apply { color = pin.foregroundArgb; style = Paint.Style.STROKE; strokeWidth = pin.strokeWidth }
    canvas.drawPath(path, paint)
    val baseline = pin.bodyHeight / 2 - (text.fontMetrics.ascent + text.fontMetrics.descent) / 2
    canvas.drawText(name, (width - text.measureText(name)) / 2, baseline, text)
    return image
}
