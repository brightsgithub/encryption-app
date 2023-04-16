package com.owusu.userdemo

import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 *
 * PURPOSE of this program
 *
 * To encrypt TEXT and place it on the Bitcoin Blockchain via ordinal inscription.
 * Why? In case you are unfairly "Cancelled" in today's society and you get locked out of your email
 * or important online places where you store important text. Simply use this Kotlin program to
 * encrypt your text and place it on the block chain.
 *
 * You just need to create remember 3 things to encrypt/decrypt your text:
 *
 * passphrase: String,
 * salt: String,
 * iterationCount: Int (recommended to use a number higher than 10,000)
 *
 * (Note: as of writing this, the current text limit of text is 60kb for an ordinal text inscription
 * on the block chain)
 *
 * This code is NOT on GitHub because there is counterparty risk in me being "cancelled" etc. from
 * GitHub. The Bitcoin Blockchain will be here forever.
 *
 * Tick-tok next block ;)
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
 */
class AESEncryption3 {

    private lateinit var key: SecretKeySpec
    private lateinit var ivParameterSpec: IvParameterSpec

    fun initKeyAndIvFromPassphrase(passphrase: String, salt: String, iterationCount: Int) {
        val keyLength = 256

        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec: KeySpec = PBEKeySpec(passphrase.toCharArray(), salt.toByteArray(), iterationCount, keyLength)
        val secretKey = keyFactory.generateSecret(keySpec)
        key = SecretKeySpec(secretKey.encoded, "AES")

        val iv = ByteArray(16) // Use a fixed IV
        ivParameterSpec = IvParameterSpec(iv)
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(cipherText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText))
        return String(decryptedBytes)
    }
}