package com.sevagrnbv.cityselection.core.designsystem.resource

import android.content.Context
import android.graphics.Typeface
import androidx.core.net.toUri
import cityselection.shared.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Загружает общий шрифт для нативного Canvas один раз, без копирования в файловый кеш. */
internal object AndroidFonts {
    private val mutex = Mutex()
    private var semiBold: Typeface? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun robotoSemiBold(context: Context): Typeface = withContext(Dispatchers.IO) {
        mutex.withLock {
            semiBold ?: run {
                val uri = Res.getUri("font/roboto_semibold.ttf").toUri()
                val path = requireNotNull(uri.path)
                // Res.getUri возвращает file:///android_asset/…, AssetManager нужен относительный путь.
                require(uri.scheme == "file" && path.startsWith(ASSET_PREFIX)) {
                    "Expected an Android asset URI for the bundled font"
                }
                Typeface.createFromAsset(context.applicationContext.assets, path.removePrefix(ASSET_PREFIX))
                    .also { semiBold = it }
            }
        }
    }

    private const val ASSET_PREFIX = "/android_asset/"
}
