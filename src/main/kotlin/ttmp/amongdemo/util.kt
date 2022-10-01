package ttmp.amongdemo

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

// fuck you jetbrains, fuck you compose desktop, fuck you kotlin, fuck you java, fuck everything in general
@Composable
fun img(resourcePath: String): Painter {
    val image = remember(resourcePath) {
        useResource(resourcePath) { loadImageBitmap(it) }
    }
    return BitmapPainter(image, filterQuality = FilterQuality.None)
}

@Composable
fun Hyperlink(uri: URI, text: String, fontSize: TextUnit = TextUnit.Unspecified) {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        TextButton(
            onClick = {
                try {
                    desktop.browse(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        ) { Text(text, fontSize = fontSize) }
    }
}

@Composable
fun HorizontalDiv() {
    Divider(modifier = Modifier.fillMaxWidth().height(1.dp))
}

@Composable
fun VerticalDiv() {
    Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
}
