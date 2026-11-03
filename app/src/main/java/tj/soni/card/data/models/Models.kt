package tj.soni.card.data.models

enum class TxType { INCOMING, OUTGOING }

enum class TxCategory {
    TRANSFER, PAYMENT, TOPUP, WITHDRAW, SALARY, SHOPPING, FOOD, TRANSPORT, UTILITIES
}

/** A SIMULATED transaction. All transactions in SONI CARD V1.0 are DEMO data. */
data class Transaction(
    val id: String,
    val counterparty: String,
    val category: TxCategory,
    val type: TxType,
    val amount: Double,
    val timestamp: Long,
    val comment: String = ""
)

/** A DEMO card. Only a masked number is ever stored or shown. */
data class DemoCard(
    val id: String,
    val nickname: String,
    val lastFour: String,
    val expiry: String,
    val frozen: Boolean = false,
    val primary: Boolean = false
)

data class AppNotification(
    val id: Int,
    val titleRes: Int,
    val bodyRes: Int,
    val read: Boolean = false
)

data class TransferDraft(
    val recipient: String,
    val recipientIsCard: Boolean,
    val amount: Double,
    val comment: String
)

data class PrefsState(
    val onboarded: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val hasPin: Boolean = false,
    val pinSalt: String = "",
    val pinHash: String = "",
    val biometricEnabled: Boolean = false,
    val balanceHidden: Boolean = false,
    val notificationsOn: Boolean = true
)
