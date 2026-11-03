package tj.soni.card.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import tj.soni.card.data.models.PrefsState
import tj.soni.card.security.KeystoreCipher

private val Context.soniStore: DataStore<Preferences> by preferencesDataStore(name = "soni_prefs")

/** Local settings. The PIN hash is additionally encrypted with an Android Keystore key. */
class Prefs(private val context: Context) {
    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val NAME = stringPreferencesKey("name")
        val PHONE = stringPreferencesKey("phone")
        val PIN_BLOB = stringPreferencesKey("pin_blob")
        val BIOMETRIC = booleanPreferencesKey("biometric")
        val BALANCE_HIDDEN = booleanPreferencesKey("balance_hidden")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
    }

    val state: Flow<PrefsState> = context.soniStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            val decrypted = p[Keys.PIN_BLOB]?.let { KeystoreCipher.decrypt(it) }
            val parts = decrypted?.split(":")
            val salt = if (parts != null && parts.size == 2) parts[0] else ""
            val hash = if (parts != null && parts.size == 2) parts[1] else ""
            PrefsState(
                onboarded = p[Keys.ONBOARDED] ?: false,
                name = p[Keys.NAME] ?: "",
                phone = p[Keys.PHONE] ?: "",
                hasPin = salt.isNotEmpty() && hash.isNotEmpty(),
                pinSalt = salt,
                pinHash = hash,
                biometricEnabled = p[Keys.BIOMETRIC] ?: false,
                balanceHidden = p[Keys.BALANCE_HIDDEN] ?: false,
                notificationsOn = p[Keys.NOTIFICATIONS] ?: true
            )
        }

    suspend fun setOnboarded(v: Boolean) { context.soniStore.edit { it[Keys.ONBOARDED] = v } }

    suspend fun setProfile(name: String, phone: String) {
        context.soniStore.edit {
            it[Keys.NAME] = name
            it[Keys.PHONE] = phone
        }
    }

    /** Returns false when the Keystore is unavailable and nothing was saved. */
    suspend fun setPin(salt: String, hash: String): Boolean {
        val blob = KeystoreCipher.encrypt("$salt:$hash") ?: return false
        context.soniStore.edit { it[Keys.PIN_BLOB] = blob }
        return true
    }

    suspend fun setBiometric(v: Boolean) { context.soniStore.edit { it[Keys.BIOMETRIC] = v } }
    suspend fun setBalanceHidden(v: Boolean) { context.soniStore.edit { it[Keys.BALANCE_HIDDEN] = v } }
    suspend fun setNotifications(v: Boolean) { context.soniStore.edit { it[Keys.NOTIFICATIONS] = v } }
    suspend fun clearAll() { context.soniStore.edit { it.clear() } }
}
