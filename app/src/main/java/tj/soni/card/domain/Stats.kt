package tj.soni.card.domain

import tj.soni.card.data.models.Transaction
import tj.soni.card.data.models.TxCategory
import tj.soni.card.data.models.TxType

data class Summary(val income: Double, val expense: Double)

object Stats {
    const val DAY_MS = 24L * 60L * 60L * 1000L

    fun summary(list: List<Transaction>): Summary = Summary(
        income = list.filter { it.type == TxType.INCOMING }.sumOf { it.amount },
        expense = list.filter { it.type == TxType.OUTGOING }.sumOf { it.amount }
    )

    fun filterSince(list: List<Transaction>, since: Long): List<Transaction> =
        list.filter { it.timestamp >= since }

    /** Outgoing totals per category, biggest first. */
    fun byCategory(list: List<Transaction>): List<Pair<TxCategory, Double>> =
        list.filter { it.type == TxType.OUTGOING }
            .groupBy { it.category }
            .map { (cat, items) -> cat to items.sumOf { it.amount } }
            .sortedByDescending { it.second }

    /** Daily totals, oldest first. The last item is the day containing [now]. */
    fun dailyTotals(list: List<Transaction>, days: Int, now: Long, type: TxType): List<Double> {
        val result = DoubleArray(days)
        for (tx in list) {
            if (tx.type != type) continue
            val ago = ((now - tx.timestamp) / DAY_MS).toInt()
            if (ago in 0 until days) result[days - 1 - ago] += tx.amount
        }
        return result.toList()
    }

    /** Filters by text (name, comment, id, amount) and an optional type. */
    fun search(list: List<Transaction>, query: String, type: TxType?): List<Transaction> {
        val q = query.trim().lowercase()
        return list.filter { tx ->
            (type == null || tx.type == type) &&
                (q.isEmpty() ||
                    tx.counterparty.lowercase().contains(q) ||
                    tx.comment.lowercase().contains(q) ||
                    tx.id.lowercase().contains(q) ||
                    String.format(java.util.Locale.US, "%.2f", tx.amount).contains(q))
        }
    }
}
