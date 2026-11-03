package tj.soni.card

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.soni.card.security.Hex
import tj.soni.card.security.PinHasher

class PinHasherTest {
    @Test fun verifyCorrectAndWrongPin() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("1234", salt)
        assertTrue(PinHasher.verify("1234", salt, hash))
        assertFalse(PinHasher.verify("4321", salt, hash))
        assertFalse(PinHasher.verify("1234", "", hash))
    }

    @Test fun saltsDiffer() {
        assertNotEquals(PinHasher.newSalt(), PinHasher.newSalt())
    }

    @Test fun hexRoundTrip() {
        val bytes = byteArrayOf(0, 1, 127, -1, -128)
        assertEquals(bytes.toList(), Hex.decode(Hex.encode(bytes)).toList())
    }
}
