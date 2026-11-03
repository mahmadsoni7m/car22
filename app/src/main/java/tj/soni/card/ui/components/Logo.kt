package tj.soni.card.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.IOException
import tj.soni.card.R
import tj.soni.card.ui.theme.SoniColors

/**
 * Loads optional logo files from app/src/main/assets/logos/<path>.(png|webp|jpg).
 * If a file is missing, the app shows a neutral icon instead. See LOGOS.md.
 */
object LogoLoader {
    private val cache = HashMap<String, ImageBitmap?>()
    private val extensions = listOf("png", "webp", "jpg", "jpeg")

    fun load(context: Context, path: String): ImageBitmap? {
        synchronized(cache) {
            if (cache.containsKey(path)) return cache[path]
        }
        var result: ImageBitmap? = null
        for (ext in extensions) {
            try {
                context.assets.open("logos/$path.$ext").use { stream ->
                    result = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
                if (result != null) break
            } catch (e: IOException) {
                // file not present: try the next extension
            }
        }
        synchronized(cache) { cache[path] = result }
        return result
    }
}

@Composable
fun rememberLogo(path: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(path) { LogoLoader.load(context, path) }
}

/** Rounded square: the logo file if it exists, otherwise the fallback icon. */
@Composable
fun LogoBox(
    path: String,
    fallback: ImageVector,
    boxSize: Dp = 48.dp,
    tint: Color = SoniColors.Teal,
    background: Color = SoniColors.Field,
    corner: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    val bmp = rememberLogo(path)
    Box(
        modifier.size(boxSize).background(background, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center
    ) {
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(boxSize * 0.1f)
            )
        } else {
            Icon(fallback, contentDescription = null, tint = tint, modifier = Modifier.size(boxSize * 0.5f))
        }
    }
}

/** The main app logo: assets/logos/app/app_logo.png if present, otherwise the built-in mark. */
@Composable
fun AppLogo(logoSize: Dp, modifier: Modifier = Modifier) {
    val bmp = rememberLogo("app/app_logo")
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.Fit,
            modifier = modifier.size(logoSize)
        )
    } else {
        SoniLogoMark(logoSize, modifier)
    }
}
