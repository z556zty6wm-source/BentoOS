package os.bento.filemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import os.bento.filemanager.ui.FileManagerRoot
import os.bento.filemanager.ui.theme.BentoFileTheme
import os.bento.settings.ui.theme.BentoColors

class FileManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BentoFileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BentoColors.bg
                ) {
                    FileManagerRoot()
                }
            }
        }
    }
}
