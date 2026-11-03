package tj.soni.card.domain

object Validators {
    const val MAX_AMOUNT = 1_000_000.0
    private val amountRegex = Regex("^\\d{1,9}([.,]\\d{1,2})?$")

    fun digitsOnly(s: String): String = s.filter { it.isDigit() }

    /** Tajik mobile number without country code: exactly 9 digits. */
    fun isValidPhone(digits: String): Boolean = digits.length == 9 && digits.all { it.isDigit() }

    fun isValidName(name: String): Boolean {
        val t = name.trim()
        return t.length in 2..50 && t.any { it.isLetter() }
    }

    fun isValidCardNumber(digits: String): Boolean = digits.length == 16 && digits.all { it.isDigit() }

    fun isValidPin(pin: String): Boolean = pin.length == 4 && pin.all { it.isDigit() }

    fun isValidSmsCode(code: String): Boolean = code.length == 6 && code.all { it.isDigit() }

    /** Returns a positive amount (max 2 decimals) or null when the text is invalid. */
    fun parseAmount(text: String): Double? {
        val t = text.trim()
        if (!amountRegex.matches(t)) return null
        val v = t.replace(',', '.').toDoubleOrNull() ?: return null
        return if (v > 0.0 && v <= MAX_AMOUNT) v else null
    }

    fun isValidMessage(text: String): Boolean = text.trim().length in 5..500
}
