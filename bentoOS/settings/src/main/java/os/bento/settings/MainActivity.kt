package os.bento.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import os.bento.settings.ui.theme.BentoSettingsTheme
import os.bento.settings.ui.theme.BentoColors
import os.bento.settings.ui.SettingsRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BentoSettingsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BentoColors.bg
                ) {
                    SettingsRoot()
                }
            }
        }
    }
}
