package tj.big.card

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import tj.big.card.localization.LocaleManager
import tj.big.card.navigation.BigApp
import tj.big.card.ui.theme.BigColors
import tj.big.card.ui.theme.BigTheme

/** AppCompatActivity is required by BiometricPrompt and per-app language switching. */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager.ensureDefault()
        setContent {
            BigTheme {
                Surface(Modifier.fillMaxSize(), color = BigColors.Background) {
                    BigApp()
                }
            }
        }
    }
}
