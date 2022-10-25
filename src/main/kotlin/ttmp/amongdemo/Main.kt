package ttmp.amongdemo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

@Composable
@Preview
fun App() {
    var code by remember { mutableStateOf("// Type among code here...") }
    var analysis by remember { mutableStateOf(listOf<Card>()) }

    var showCode by remember { mutableStateOf(true) }
    var showAnalysis by remember { mutableStateOf(true) }

    val compileScope = rememberCoroutineScope()
    var currentJob by remember { mutableStateOf<Job?>(null) }
    val increment by remember { mutableStateOf(AtomicInteger()) }

    Row(modifier = Modifier.background(Color.LightGray)) {
        Column(
            modifier = Modifier.fillMaxHeight().width(50.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(checked = showCode, onCheckedChange = { showCode = it })
                Switch(checked = showAnalysis, onCheckedChange = { showAnalysis = it })
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Hyperlink(URI.create("https://github.com/TTMP-Modding-Team/Among/wiki"), "Docs", fontSize = 10.sp)
                IconButton(onClick = { ScreenManager.aboutScreen() }) {
                    Image(
                        img("logo.png"),
                        "Logo",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFDDDDDD))
        ) {
            if (showCode) {
                TextField(
                    code,
                    onValueChange = {
                        code = it
                        currentJob?.cancel()
                        val currentIncrement = increment.incrementAndGet()
                        if (it.isBlank()) {
                            analysis = listOf()
                        } else {
                            //println("Queueing new compilation, increment = $currentIncrement")
                            currentJob = compileScope.launch {
                                delay(500)
                                if (currentIncrement == increment.get()) {
                                    // println("Triggering compilation, increment = $currentIncrement")
                                    val cards = amongToCards(it)
                                    analysis = cards
                                    //println("New cards: $cards")
                                }
                            }
                        }
                    },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                    modifier = Modifier.padding(10.dp).fillMaxSize()
                )
            }
            if (showAnalysis) {
                Box(modifier = Modifier
                    .zIndex(1f)
                    .let {
                        if (showCode) it.width(300.dp).align(Alignment.TopEnd)
                        else it.fillMaxWidth()
                    }.fillMaxHeight()
                    .background(Color.White)
                    .clickable(remember { MutableInteractionSource() }, null) {}) {
                    if (showCode) Divider(modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().width(1.dp))

                    val scroll = rememberScrollState(0)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("Analysis", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 20.dp))
                        Box(modifier = Modifier.fillMaxWidth().verticalScroll(scroll)) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                for (card in analysis) {
                                    key(card) {
                                        CardWidget(card)
                                    }
                                }
                            }
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(scroll)
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Among Demo",
        icon = img("logo.png")
    ) {
        App()
    }

    for (window in ScreenManager.windows) {
        key(window) {
            window.compose(this) { ScreenManager.closeScreen(window.id) }
        }
    }
}