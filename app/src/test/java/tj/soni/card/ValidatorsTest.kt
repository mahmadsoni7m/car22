package tj.soni.card

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.soni.card.domain.Validators

class ValidatorsTest {
    @Test fun phone() {
        assertTrue(Validators.isValidPhone("901234567"))
        assertFalse(Validators.isValidPhone("90123456"))
        assertFalse(Validators.isValidPhone("90123456a"))
    }

    @Test fun amount() {
        assertEquals(10.5, Validators.parseAmount("10,5")!!, 0.0001)
        assertEquals(100.0, Validators.parseAmount("100")!!, 0.0001)
        assertNull(Validators.parseAmount("0"))
        assertNull(Validators.parseAmount("-5"))
        assertNull(Validators.parseAmount("1.234"))
        assertNull(Validators.parseAmount("abc"))
        assertNull(Validators.parseAmount("2000000"))
    }

    @Test fun pinAndSms() {
        assertTrue(Validators.isValidPin("0420"))
        assertFalse(Validators.isValidPin("042"))
        assertTrue(Validators.isValidSmsCode("123456"))
        assertFalse(Validators.isValidSmsCode("12345"))
    }

    @Test fun cardAndName() {
        assertTrue(Validators.isValidCardNumber("1234567890123456"))
        assertFalse(Validators.isValidCardNumber("1234"))
        assertTrue(Validators.isValidName("Анвар"))
        assertFalse(Validators.isValidName("A"))
        assertFalse(Validators.isValidName("1234"))
    }

    @Test fun digitsOnly() {
        assertEquals("123", Validators.digitsOnly("1a2-3"))
    }
}
