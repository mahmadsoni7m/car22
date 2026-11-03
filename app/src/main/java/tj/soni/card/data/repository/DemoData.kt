package tj.soni.card.data.repository

import tj.soni.card.R
import tj.soni.card.data.models.AppNotification
import tj.soni.card.data.models.DemoCard
import tj.soni.card.data.models.Transaction
import tj.soni.card.data.models.TxCategory
import tj.soni.card.data.models.TxCategory.*
import tj.soni.card.data.models.TxType
import tj.soni.card.data.models.TxType.*

/** Fixed DEMO data. Nothing here is real money, a real account or a real card. */
object DemoData {
    const val INITIAL_BALANCE = 12450.75
    private const val DAY = 24L * 60L * 60L * 1000L

    fun transactions(now: Long): List<Transaction> {
        fun t(n: Int, name: String, c: TxCategory, type: TxType, amount: Double, daysAgo: Double, comment: String = "") =
            Transaction(
                id = "DEMO-" + (1000 + n),
                counterparty = name,
                category = c,
                type = type,
                amount = amount,
                timestamp = now - (daysAgo * DAY).toLong(),
                comment = comment
            )
        return listOf(
            t(18, "Fresh Market", FOOD, OUTGOING, 126.40, 0.2),
            t(17, "Madina S.", TRANSFER, INCOMING, 300.00, 0.9, "Thanks!"),
            t(16, "City Transport", TRANSPORT, OUTGOING, 18.00, 1.3),
            t(15, "Aqua Utilities", UTILITIES, OUTGOING, 74.50, 2.1),
            t(14, "Demo Employer", SALARY, INCOMING, 4200.00, 3.0, "Monthly salary"),
            t(13, "Rustam K.", TRANSFER, OUTGOING, 500.00, 4.2, "Rent share"),
            t(12, "Mobile Plan", PAYMENT, OUTGOING, 45.00, 5.1),
            t(11, "Home Style Store", SHOPPING, OUTGOING, 389.90, 6.4),
            t(10, "Cafe Navruz", FOOD, OUTGOING, 92.00, 8.0),
            t(9, "Top up (demo)", TOPUP, INCOMING, 1000.00, 10.0),
            t(8, "Power Grid Demo", UTILITIES, OUTGOING, 160.20, 12.0),
            t(7, "City Transport", TRANSPORT, OUTGOING, 24.00, 14.0),
            t(6, "Bookshop", SHOPPING, OUTGOING, 210.00, 16.0),
            t(5, "Farrukh A.", TRANSFER, INCOMING, 650.00, 19.0),
            t(4, "ATM (demo)", WITHDRAW, OUTGOING, 800.00, 21.0),
            t(3, "Fresh Market", FOOD, OUTGOING, 143.75, 24.0),
            t(2, "Internet Plan", PAYMENT, OUTGOING, 120.00, 27.0),
            t(1, "Demo Employer", SALARY, INCOMING, 4200.00, 33.0, "Monthly salary")
        )
    }

    fun cards(): List<DemoCard> = listOf(
        DemoCard("card1", "SONI Silver", "4821", "09/29", frozen = false, primary = true),
        DemoCard("card2", "Online Shopping", "7316", "03/28", frozen = false, primary = false)
    )

    fun notifications(): List<AppNotification> = listOf(
        AppNotification(1, R.string.notif1_title, R.string.notif1_body),
        AppNotification(2, R.string.notif2_title, R.string.notif2_body),
        AppNotification(3, R.string.notif3_title, R.string.notif3_body),
        AppNotification(4, R.string.notif4_title, R.string.notif4_body, read = true)
    )
}
