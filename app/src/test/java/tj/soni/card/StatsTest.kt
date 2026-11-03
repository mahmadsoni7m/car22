package tj.soni.card

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.soni.card.data.models.Transaction
import tj.soni.card.data.models.TxCategory
import tj.soni.card.data.models.TxType
import tj.soni.card.domain.Stats

class StatsTest {
    private val now = 10L * Stats.DAY_MS
    private fun tx(id: String, type: TxType, amt: Double, daysAgo: Int, cat: TxCategory = TxCategory.FOOD, name: String = "Shop") =
        Transaction(id, name, cat, type, amt, now - daysAgo * Stats.DAY_MS)

    private val list = listOf(
        tx("A", TxType.INCOMING, 100.0, 0, TxCategory.SALARY, "Employer"),
        tx("B", TxType.OUTGOING, 30.0, 0),
        tx("C", TxType.OUTGOING, 20.0, 1, TxCategory.TRANSPORT, "Bus"),
        tx("D", TxType.OUTGOING, 5.0, 9)
    )

    @Test fun summary() {
        val s = Stats.summary(list)
        assertEquals(100.0, s.income, 0.001)
        assertEquals(55.0, s.expense, 0.001)
    }

    @Test fun byCategorySortedDesc() {
        val c = Stats.byCategory(list)
        assertEquals(TxCategory.FOOD, c[0].first)
        assertEquals(35.0, c[0].second, 0.001)
    }

    @Test fun dailyTotals() {
        val d = Stats.dailyTotals(list, 7, now, TxType.OUTGOING)
        assertEquals(7, d.size)
        assertEquals(30.0, d[6], 0.001)
        assertEquals(20.0, d[5], 0.001)
        assertEquals(0.0, d[0], 0.001)
    }

    @Test fun searchAndFilter() {
        assertEquals(1, Stats.search(list, "bus", null).size)
        assertEquals(3, Stats.search(list, "", TxType.OUTGOING).size)
        assertEquals(0, Stats.search(list, "zzz", null).size)
        assertEquals(1, Stats.search(list, "100.00", null).size)
    }

    @Test fun filterSince() {
        assertEquals(3, Stats.filterSince(list, now - 2 * Stats.DAY_MS).size)
    }
}
