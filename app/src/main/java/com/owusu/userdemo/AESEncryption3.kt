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
In the context of encryption and security, the importance and confidentiality of the following
elements can be summarized as follows:

Passphrase (or encryption key):Most Important:
The passphrase, or encryption key, is the most critical element to keep secret.
It's the primary piece of information that should never be revealed. It's the key to decrypting
the data, and its confidentiality is essential for the security of the encrypted content.

Iteration Count: Important:
The iteration count is not a secret like the passphrase, but it's crucial for key derivation.
It should be a high number to slow down brute-force attacks. While it doesn't need to be kept secret,
it should not be changed without careful consideration, as it affects the derived key.

Salt: Important for Security:
The salt is typically not a secret but is important for enhancing security.
It's often stored in plain text alongside the ciphertext. It helps ensure that the same passphrase
results in different keys, making precomputed attacks less effective.
It should be unique for each encryption operation.

IV (Initialization Vector): Public:
The IV is generally considered public information and is often stored with the ciphertext.
Its primary purpose is to introduce randomness and prevent patterns in the ciphertext.
It is not a secret, and it is essential for secure encryption and decryption.
It should not be changed without proper handling.
In summary, while the passphrase should always remain secret, the other elements
(iteration count, salt, and IV) serve different purposes in encryption and can often be stored in
plain text without compromising security. Their values should be carefully managed and remain
consistent during encryption and decryption to ensure the security and integrity of the encrypted data.
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

    private fun createSecretKeySpec(
        passphrase: String,
        iterationCount: Int,
        salt: ByteArray
    ): SecretKeySpec {
        val keyLength = 256
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec: KeySpec = PBEKeySpec(passphrase.toCharArray(), salt, iterationCount, keyLength)
        val secretKey = keyFactory.generateSecret(keySpec)
        return SecretKeySpec(secretKey.encoded, ALGORITHM)
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
