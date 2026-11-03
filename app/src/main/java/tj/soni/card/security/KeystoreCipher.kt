package tj.soni.card.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** AES-GCM encryption with a non-exportable key kept in the Android Keystore. */
object KeystoreCipher {
    private const val PROVIDER = "AndroidKeyStore"
    private const val ALIAS = "soni_card_secure_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private fun secretKey(): SecretKey {
        val store = KeyStore.getInstance(PROVIDER).apply { load(null) }
        val existing = store.getKey(ALIAS, null)
        if (existing is SecretKey) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    /** Returns "ivHex:cipherHex" or null if encryption is unavailable. */
    fun encrypt(plain: String): String? = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        Hex.encode(cipher.iv) + ":" + Hex.encode(encrypted)
    } catch (e: Exception) {
        null
    }

    fun decrypt(blob: String): String? = try {
        val parts = blob.split(":")
        if (parts.size != 2) {
            null
        } else {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, Hex.decode(parts[0])))
            String(cipher.doFinal(Hex.decode(parts[1])), Charsets.UTF_8)
        }
    } catch (e: Exception) {
        null
    }
}
