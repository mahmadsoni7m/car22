package tj.soni.card.data.repository

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tj.soni.card.data.models.AppNotification
import tj.soni.card.data.models.DemoCard
import tj.soni.card.data.models.Transaction
import tj.soni.card.data.models.TransferDraft
import tj.soni.card.data.models.TxCategory
import tj.soni.card.data.models.TxType

/**
 * In-memory repository for the offline DEMO. Every operation only changes
 * simulated numbers; nothing is sent anywhere.
 */
class DemoRepository(private val clock: () -> Long = { System.currentTimeMillis() }) {
    private val lock = Any()
    private val counter = AtomicInteger(2000)

    private val _transactions = MutableStateFlow(DemoData.transactions(clock()))
    private val _balance = MutableStateFlow(DemoData.INITIAL_BALANCE)
    private val _cards = MutableStateFlow(DemoData.cards())
    private val _notifications = MutableStateFlow(DemoData.notifications())

    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    val balance: StateFlow<Double> = _balance.asStateFlow()
    val cards: StateFlow<List<DemoCard>> = _cards.asStateFlow()
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun reset() {
        synchronized(lock) {
            _transactions.value = DemoData.transactions(clock())
            _balance.value = DemoData.INITIAL_BALANCE
            _cards.value = DemoData.cards()
            _notifications.value = DemoData.notifications()
        }
    }

    fun findTransaction(id: String): Transaction? = _transactions.value.firstOrNull { it.id == id }

    fun transfer(draft: TransferDraft): Transaction? =
        spend(TxCategory.TRANSFER, maskRecipient(draft), draft.amount, draft.comment)

    fun pay(merchant: String, amount: Double, category: TxCategory = TxCategory.PAYMENT): Transaction? =
        spend(category, merchant, amount, "")

    fun withdraw(amount: Double, method: String): Transaction? =
        spend(TxCategory.WITHDRAW, method, amount, "")

    fun topUp(amount: Double, source: String): Transaction = synchronized(lock) {
        val tx = newTx(TxCategory.TOPUP, source, TxType.INCOMING, amount, "")
        _balance.value = _balance.value + amount
        _transactions.update { listOf(tx) + it }
        tx
    }

    private fun spend(category: TxCategory, name: String, amount: Double, comment: String): Transaction? =
        synchronized(lock) {
            if (amount <= 0.0 || amount > _balance.value) return null
            val tx = newTx(category, name, TxType.OUTGOING, amount, comment)
            _balance.value = _balance.value - amount
            _transactions.update { listOf(tx) + it }
            tx
        }

    private fun newTx(c: TxCategory, name: String, type: TxType, amount: Double, comment: String) =
        Transaction(
            id = "DEMO-" + counter.incrementAndGet(),
            counterparty = name,
            category = c,
            type = type,
            amount = amount,
            timestamp = clock(),
            comment = comment
        )

    fun toggleFreeze(cardId: String) {
        _cards.update { list -> list.map { if (it.id == cardId) it.copy(frozen = !it.frozen) else it } }
    }

    fun addCard(nickname: String): DemoCard = synchronized(lock) {
        val n = _cards.value.size + 1
        val card = DemoCard(
            id = "card$n",
            nickname = nickname.trim(),
            lastFour = (1000 + n * 111).toString().takeLast(4),
            expiry = "12/29"
        )
        _cards.update { it + card }
        card
    }

    fun markNotificationRead(id: Int) {
        _notifications.update { list -> list.map { if (it.id == id) it.copy(read = true) else it } }
    }

    fun markAllRead() {
        _notifications.update { list -> list.map { it.copy(read = true) } }
    }

    companion object {
        /** Never keep the full recipient: store a masked label only. */
        fun maskRecipient(d: TransferDraft): String =
            if (d.recipientIsCard) "**** " + d.recipient.takeLast(4)
            else "+992 *** " + d.recipient.takeLast(3)
    }
}
