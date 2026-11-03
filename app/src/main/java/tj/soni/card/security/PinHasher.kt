package tj.soni.card.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PBKDF2 hashing for the app PIN. The PIN itself is never stored. */
object PinHasher {
    private const val ITERATIONS = 60_000
    private const val KEY_BITS = 256

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Hex.encode(bytes)
    }

    fun hash(pin: String, saltHex: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), Hex.decode(saltHex), ITERATIONS, KEY_BITS)
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            return Hex.encode(factory.generateSecret(spec).encoded)
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(pin: String, saltHex: String, expectedHash: String): Boolean {
        if (saltHex.isEmpty() || expectedHash.isEmpty()) return false
        val actual = hash(pin, saltHex)
        return MessageDigest.isEqual(actual.toByteArray(), expectedHash.toByteArray())
    }
}
