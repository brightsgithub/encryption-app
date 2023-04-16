package com.owusu.userdemo

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * This cares about white spaces before and after the entered text and so do these sites:
 *
 * https://codebeautify.org/sha256-hash-generator
 * https://www.movable-type.co.uk/scripts/sha256.html
 * https://xorbin.com/tools/sha256-hash-calculator
 * https://emn178.github.io/online-tools/sha256.html
 *
 * USES A SALT
 * https://www.symbionts.de/tools/hash/sha256-hash-salt-generator.html
 */
class HashUtils {

    /**
     * This method takes a plaintext input and a salt as input, and applies SHA-256 hashing to the
     * plaintext input using the salt. It returns the resulting hashed value as a hex
     */
    fun hashTextWithSalt(text: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashedBytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    /**
     *  This method generates a random salt using the SecureRandom class.
     *  The size of the salt is 16 bytes.
     */
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    fun generateSalt(providedSalt: String): ByteArray {
        return providedSalt.toByteArray()
    }

    /**
     * This is a helper method that converts a byte array to a hex string for easier display and storage.
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}