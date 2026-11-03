package tj.soni.card

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.soni.card.data.models.TransferDraft
import tj.soni.card.data.models.TxType
import tj.soni.card.data.repository.DemoData
import tj.soni.card.data.repository.DemoRepository

class DemoRepositoryTest {
    @Test fun transferReducesBalanceAndAddsTransaction() {
        val repo = DemoRepository()
        val before = repo.transactions.value.size
        val tx = repo.transfer(TransferDraft("901234567", false, 100.0, "hi"))
        assertNotNull(tx)
        assertEquals(DemoData.INITIAL_BALANCE - 100.0, repo.balance.value, 0.001)
        assertEquals(before + 1, repo.transactions.value.size)
        assertEquals(TxType.OUTGOING, repo.transactions.value.first().type)
        assertFalse(repo.transactions.value.first().counterparty.contains("901234567"))
    }

    @Test fun cannotSpendMoreThanBalance() {
        val repo = DemoRepository()
        assertNull(repo.withdraw(DemoData.INITIAL_BALANCE + 1, "ATM"))
        assertEquals(DemoData.INITIAL_BALANCE, repo.balance.value, 0.001)
    }

    @Test fun topUpIncreasesBalance() {
        val repo = DemoRepository()
        repo.topUp(50.0, "Demo")
        assertEquals(DemoData.INITIAL_BALANCE + 50.0, repo.balance.value, 0.001)
    }

    @Test fun freezeToggleAndReset() {
        val repo = DemoRepository()
        repo.toggleFreeze("card1")
        assertTrue(repo.cards.value.first { it.id == "card1" }.frozen)
        repo.addCard("Travel")
        assertEquals(3, repo.cards.value.size)
        repo.reset()
        assertFalse(repo.cards.value.first { it.id == "card1" }.frozen)
        assertEquals(2, repo.cards.value.size)
    }

    @Test fun maskedRecipientNeverContainsFullNumber() {
        val m = DemoRepository.maskRecipient(TransferDraft("1234567890123456", true, 1.0, ""))
        assertEquals("**** 3456", m)
    }
}
