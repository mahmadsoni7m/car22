package tj.soni.card

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import tj.soni.card.localization.LocaleManager
import tj.soni.card.navigation.SoniApp
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.ui.theme.SoniTheme

/** AppCompatActivity is required by BiometricPrompt and per-app language switching. */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager.ensureDefault()
        setContent {
            SoniTheme {
                Surface(Modifier.fillMaxSize(), color = SoniColors.Background) {
                    SoniApp()
                }
            }
        }
    }
}
