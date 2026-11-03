package tj.soni.card.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tj.soni.card.data.local.Prefs
import tj.soni.card.data.models.DemoCard
import tj.soni.card.data.models.PrefsState
import tj.soni.card.data.models.Transaction
import tj.soni.card.data.models.TransferDraft
import tj.soni.card.data.models.TxCategory
import tj.soni.card.data.repository.DemoRepository
import tj.soni.card.security.PinHasher

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val repo = DemoRepository()

    /** null until the first read from DataStore finishes. */
    val settings: StateFlow<PrefsState?> = prefs.state
        .map<PrefsState, PrefsState?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val transactions = repo.transactions
    val balance = repo.balance
    val cards = repo.cards
    val notifications = repo.notifications

    /** True after a successful PIN / biometric login in this app session. */
    var unlocked by mutableStateOf(false)
        private set

    var pendingPhone by mutableStateOf("")
    var pendingName by mutableStateOf("")
    var pendingIsRegistration by mutableStateOf(false)
    var pendingTransfer by mutableStateOf<TransferDraft?>(null)

    /** Quick-access shortcuts on the home screen (session only). */
    val favorites = mutableStateListOf(*DEFAULT_FAVORITES.toTypedArray())

    /** Last recipients, session only: "p:<9 digits>" or "c:<16 digits>". */
    val recentRecipients = mutableStateListOf<String>()

    fun toggleFavorite(key: String) {
        if (!favorites.remove(key)) favorites.add(key)
    }

    fun removeFavorite(key: String) {
        favorites.remove(key)
    }

    fun rememberRecipient(entry: String) {
        recentRecipients.remove(entry)
        recentRecipients.add(0, entry)
        while (recentRecipients.size > 5) recentRecipients.removeAt(recentRecipients.size - 1)
    }

    fun completeOnboarding() {
        viewModelScope.launch { prefs.setOnboarded(true) }
    }

    /** Called after the demo SMS code was accepted. */
    fun completePhoneVerification() {
        val existing = settings.value?.name.orEmpty()
        val name = when {
            pendingIsRegistration && pendingName.isNotBlank() -> pendingName.trim()
            existing.isNotBlank() -> existing
            else -> DEFAULT_NAME
        }
        val phone = pendingPhone
        viewModelScope.launch { prefs.setProfile(name, phone) }
    }

    fun unlock() { unlocked = true }
    fun lock() { unlocked = false }

    suspend fun verifyPin(pin: String): Boolean {
        val s = settings.value ?: return false
        return withContext(Dispatchers.Default) { PinHasher.verify(pin, s.pinSalt, s.pinHash) }
    }

    /** Returns false if the PIN could not be stored securely. */
    suspend fun savePin(pin: String): Boolean = withContext(Dispatchers.Default) {
        val salt = PinHasher.newSalt()
        prefs.setPin(salt, PinHasher.hash(pin, salt))
    }

    fun setBiometric(enabled: Boolean) { viewModelScope.launch { prefs.setBiometric(enabled) } }
    fun setBalanceHidden(hidden: Boolean) { viewModelScope.launch { prefs.setBalanceHidden(hidden) } }
    fun setNotifications(on: Boolean) { viewModelScope.launch { prefs.setNotifications(on) } }

    fun resetDemoData() = repo.reset()

    fun wipeEverything() {
        viewModelScope.launch {
            prefs.clearAll()
            repo.reset()
            unlocked = false
            pendingTransfer = null
            favorites.clear()
            favorites.addAll(DEFAULT_FAVORITES)
            recentRecipients.clear()
        }
    }

    fun findTransaction(id: String): Transaction? = repo.findTransaction(id)
    fun transfer(d: TransferDraft): Transaction? = repo.transfer(d)
    fun pay(merchant: String, amount: Double, category: TxCategory = TxCategory.PAYMENT): Transaction? =
        repo.pay(merchant, amount, category)
    fun withdraw(amount: Double, method: String): Transaction? = repo.withdraw(amount, method)
    fun topUp(amount: Double, source: String): Transaction = repo.topUp(amount, source)
    fun toggleFreeze(cardId: String) = repo.toggleFreeze(cardId)
    fun addCard(nickname: String): DemoCard = repo.addCard(nickname)
    fun markNotificationRead(id: Int) = repo.markNotificationRead(id)
    fun markAllNotificationsRead() = repo.markAllRead()

    companion object {
        const val DEFAULT_NAME = "Demo User"
        val DEFAULT_FAVORITES = listOf("transfer:phone", "transfer:card", "svc:mobile", "topup")
    }
}
