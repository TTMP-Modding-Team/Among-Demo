package ttmp.amongdemo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState

object ScreenManager {
    val windows = mutableStateListOf<ScreenState>()
    private var internalId = 0

    fun aboutScreen() {
        windows += screenState {
            Window(
                onCloseRequest = it,
                title = "About",
                icon = img("logo.png"),
                state = WindowState(size = DpSize.Unspecified),
                resizable = false
            ) {
                About()
            }
        }
    }

    fun closeScreen(stateId: Int) {
        windows.removeIf { it.id == stateId }
    }

    private fun screenState(compose: @Composable ApplicationScope.(() -> Unit) -> Unit): ScreenState =
        ScreenState(internalId++, compose)
}

class ScreenState(
    val id: Int,
    val compose: @Composable ApplicationScope.(() -> Unit) -> Unit
)