package ttmp.amongdemo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

@Composable
@Preview
fun About() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.size(400.dp, 300.dp).padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                img("logo.png"),
                "Logo",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Among Demo")
            Spacer(modifier = Modifier.width(30.dp))
            Row {
                Text("Among v.0.1.0")
                Spacer(modifier = Modifier.width(30.dp))
                Text("Among Demo v.0.1.0")
            }
            Spacer(modifier = Modifier.width(30.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                Hyperlink(URI.create("https://github.com/TTMP-Modding-Team/Among"), "Among GitHub")
                Hyperlink(URI.create("https://github.com/TTMP-Modding-Team/Among-Demo"), "Among Demo GitHub")
            }
        }
    }
}