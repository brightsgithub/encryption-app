package com.owusu.userdemo

import android.security.keystore.KeyProperties
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 *
 *
 * Question:
 * in order to brute force the encrypted text, what role does
 * passphrase: String, salt: String, iterationCount: Int currently play?
 *
 * In the given class, the passphrase, salt, and iteration count are used to derive a symmetric
 * encryption key using the PBKDF2 algorithm. This derived key is then used to encrypt and decrypt
 * the data using the AES algorithm in CBC mode.
 * The passphrase is the user's secret input, which is used to derive the encryption key. The salt
 * is a random value that is added to the passphrase to prevent attackers from precomputing the
 * hash values of commonly used passwords. In this case however, you provide the Salt.
 * The iteration count is the number of times the hash
 * function is applied to the passphrase and salt, which slows down the key derivation process and
 * makes it harder for attackers to guess the passphrase by brute force.
 * Therefore, in order to brute force the encrypted text, an attacker would need to guess the
 * passphrase used to derive the encryption key, as well as the salt and iteration count used in
 * the PBKDF2 algorithm. This is computationally difficult and time-consuming, especially if the
 * iteration count is high.
 *
 * Random Salt
 * now generating a random salt and storing it with the encrypted data.
 * This makes the encryption more secure, as it makes it more difficult for attackers to crack the encryption.
 * A salt is not meant to be secret and it can be stored in plain text in the database.
 * It is used to ensure the hash result is unique to each password, as long as it is randomly generated.
 * this way the encrypted same text will always look different since the salt is random
 *
 * Random IV
 * Fixed Initialization Vector (IV): Using a fixed IV is not secure. An Initialization Vector should
 * be unique for each encryption operation. Reusing the same IV can lead to various security
 * vulnerabilities, including potential leakage of information.
 *
 * To successfully decrypt the text at a later date, you need to ensure that you can recreate the
 * same IV used during encryption. There are a few common approaches to handling this:

    1. Store the IV: One of the simplest approaches is to store the IV along with the encrypted data.
    When you encrypt the data, generate a random IV, use it for encryption, and then prepend it to
    the encrypted text before saving it. When you decrypt the data, you can extract the IV from
    the beginning of the encrypted text and use it in the decryption process.

    2. Use a Deterministic IV Generation: If you want to avoid storing the IV separately, you can
    use a deterministic method to generate the IV. For example, you could derive the IV from the
    passphrase and salt using a separate, well-defined algorithm that you also use for decryption.
    This way, you can recreate the same IV during both encryption and decryption.

 I am using option 1
 *
 * Question:
 * is it possible to use my own passphrase instead of a key which must be 32 bits long?
 *
 * Yes, it is possible to use a passphrase instead of a key in AES encryption.
 * This technique is called "password-based encryption" or "PBE". However, instead of simply using
 * the passphrase as the encryption key, a "key derivation function" (KDF) is applied to the
 * passphrase to generate a suitable key. The KDF takes the passphrase and additional parameters,
 * such as a salt and an iteration count, and produces a key that can be used with the encryption
 * algorithm.
 * In Java, the SecretKeyFactory class provides methods to create a secret key from a passphrase
 * using a KDF. Here's an example of how to use it:
 *
 */
class AESEncryption3 {

    fun encrypt(
        passphrase: String,
        iterationCount: Int,
        plainText: String,
        metadata: String?
    ): String {
        val salt = generateRandomSalt() // Generate a random salt
        val key = createSecretKeySpec(passphrase, iterationCount, salt)
        val ivParameterSpec = getIVSecureRandom(TRANSFORMATION)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())

        val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter: JsonAdapter<PayLoad> = moshi.adapter(PayLoad::class.java)

        // Combine the IV, salt, and the encrypted data
        val encryptedData = PayLoad(
            iv = Base64.getEncoder().encodeToString(ivParameterSpec.iv),
            data = Base64.getEncoder().encodeToString(encryptedBytes),
            salt = Base64.getEncoder().encodeToString(salt),
            metadata = metadata
        )

        // Serialize the PayLoad object to JSON
        return jsonAdapter.toJson(encryptedData)
    }

    fun decrypt(passphrase: String, iterationCount: Int, cipherText: String): DecryptedWrapperData {
        // Create a JsonAdapter for EncryptedData
        val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<PayLoad> = moshi.adapter(PayLoad::class.java)

        // Deserialize JSON into EncryptedData
        val encryptedData = adapter.fromJson(cipherText)

        val iv = Base64.getDecoder().decode(encryptedData?.iv)
        val salt = Base64.getDecoder().decode(encryptedData?.salt)
        val encryptedBytes = Base64.getDecoder().decode(encryptedData?.data)
        val metadata = encryptedData?.metadata
        val key = createSecretKeySpec(passphrase, iterationCount, salt)

        val ivParameterSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return DecryptedWrapperData(decryptedText = String(decryptedBytes), metaData = metadata)
    }

    private fun createSecretKeySpec(passphrase: String, iterationCount: Int, salt: ByteArray): SecretKeySpec {
        val keyLength = 256
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec: KeySpec = PBEKeySpec(passphrase.toCharArray(), salt, iterationCount, keyLength)
        val secretKey = keyFactory.generateSecret(keySpec)
        return  SecretKeySpec(secretKey.encoded, ALGORITHM)
    }

    companion object {

        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        // private const val PADDING = "PKCS5Padding" works with unit tests but stick to 7 and use androidTest
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private fun getIVSecureRandom(algorithm: String): IvParameterSpec {
            val random = SecureRandom.getInstanceStrong()
            val iv = ByteArray(Cipher.getInstance(algorithm).blockSize)
            random.nextBytes(iv)
            return IvParameterSpec(iv)
        }

        private fun generateRandomSalt(): ByteArray {
            val salt = ByteArray(16) // Change the size of the salt as needed
            val random = SecureRandom.getInstanceStrong()
            random.nextBytes(salt)
            return salt
        }
    }

    data class DecryptedWrapperData(val decryptedText: String, val metaData: String?)

    data class PayLoad(
        val iv: String,
        val data: String,
        val salt: String,
        val metadata: String?
    )
}
