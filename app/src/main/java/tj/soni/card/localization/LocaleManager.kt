package tj.soni.card.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Per-app language support. Supported tags: tg (Tajik), ru (Russian), en (English). */
object LocaleManager {
    const val TAJIK = "tg"
    const val RUSSIAN = "ru"
    const val ENGLISH = "en"
    val supported = listOf(TAJIK, RUSSIAN, ENGLISH)

    fun current(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return TAJIK
        val lang = locales[0]?.language ?: return TAJIK
        return if (lang in supported) lang else TAJIK
    }

    fun apply(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    /** Tajik is the primary language: use it on first launch. */
    fun ensureDefault() {
        if (AppCompatDelegate.getApplicationLocales().isEmpty) apply(TAJIK)
    }
}
