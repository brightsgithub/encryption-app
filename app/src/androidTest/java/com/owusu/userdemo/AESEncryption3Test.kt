package com.owusu.userdemo

import android.util.Log
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

class AESEncryption3Test {

    val iterationCount = 10_000
    private val PASSPHRASE = "passphrase"
    private val textToBeEncrypted = StringBuilder()

    private fun buildText(): String {
        val sb = StringBuilder()
        sb.append("\n\nthe test program\n\n")

        for (number in 1..30) {
            sb.append("\n")
            sb.append("$number. Title and subtitle that is shown $number")
        }
        return sb.toString()
    }


    @Test
    fun testEncryptionDecryption() {

        val textToBeEncrypted = buildText()
        val aes = AESEncryption3()

        // Encrypt the plain text
        val encrypted = aes.encrypt(PASSPHRASE, iterationCount, textToBeEncrypted, "some data")
        Log.v("AESEncryption3Test", "Encrypted is\n" + encrypted)

        // Decrypt the cipher text
        val decrypted = aes.decrypt(PASSPHRASE, iterationCount, encrypted)
        Log.v("AESEncryption3Test", "Decrypted is\n" + decrypted)

        // Check that the decrypted text matches the original plain text
        assertEquals(textToBeEncrypted, decrypted.decryptedText)
    }

    @Test
    fun testEncryptionAndDecryption() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val iterationCount = 10000
        val plainText = "This is a test message."
        val metadata = "Some metadata for testing."

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Encrypt the data
        val encryptedData = encryption.encrypt(passphrase, iterationCount, plainText, metadata)

        // Decrypt the data
        val decryptedData = encryption.decrypt(passphrase, iterationCount, encryptedData)

        // Verify the decrypted data matches the original plaintext
        Assert.assertEquals(plainText, decryptedData.decryptedText)
        Assert.assertEquals(metadata, decryptedData.metaData)
    }

    @Test
    fun testEncryptionWithDifferentPassphraseFails() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val wrongPassphrase = "WrongPassphrase456!"
        val iterationCount = 10000
        val plainText = "This is a test message."

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Encrypt the data with the correct passphrase
        val encryptedData = encryption.encrypt(passphrase, iterationCount, plainText, null)

        // Attempt to decrypt with the wrong passphrase
        try {
            encryption.decrypt(wrongPassphrase, iterationCount, encryptedData)
            // If the decryption succeeded, the test should fail
            Assert.fail("Decryption with wrong passphrase should fail.")
        } catch (e: Exception) {
            // If an exception is thrown, the test passes
        }
    }

    @Test
    fun testEncryptionWithDifferentIterationCountFails() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val iterationCount = 10000
        val wrongIterationCount = 5000
        val plainText = "This is a test message."

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Encrypt the data with the correct iteration count
        val encryptedData = encryption.encrypt(passphrase, iterationCount, plainText, null)

        // Attempt to decrypt with the wrong iteration count
        try {
            encryption.decrypt(passphrase, wrongIterationCount, encryptedData)
            // If the decryption succeeded, the test should fail
            Assert.fail("Decryption with wrong iteration count should fail.")
        } catch (e: Exception) {
            // If an exception is thrown, the test passes
        }
    }


    @Test
    fun testEncryptionWithNullMetadata() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val iterationCount = 10000
        val plainText = "This is a test message."

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Encrypt the data with null metadata
        val encryptedData = encryption.encrypt(passphrase, iterationCount, plainText, null)

        // Decrypt the data
        val decryptedData = encryption.decrypt(passphrase, iterationCount, encryptedData)

        // Verify the decrypted data matches the original plaintext
        Assert.assertEquals(plainText, decryptedData.decryptedText)
        Assert.assertNull(decryptedData.metaData)
    }

    @Test
    fun testEncryptionWithEmptyPlainText() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val iterationCount = 10000
        val plainText = ""

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Encrypt the empty data
        val encryptedData = encryption.encrypt(passphrase, iterationCount, plainText, null)

        // Decrypt the data
        val decryptedData = encryption.decrypt(passphrase, iterationCount, encryptedData)

        // Verify the decrypted data is an empty string
        Assert.assertEquals("", decryptedData.decryptedText)
        Assert.assertNull(decryptedData.metaData)
    }

    @Test
    fun testEncryptionWithSpecialCharacters() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val iterationCount = 10000
        val plainText = "!@#$%^&*()_+{}:\"<>?`-=[];',./"

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Encrypt data with special characters
        val encryptedData = encryption.encrypt(passphrase, iterationCount, plainText, null)

        // Decrypt the data
        val decryptedData = encryption.decrypt(passphrase, iterationCount, encryptedData)

        // Verify the decrypted data matches the original plaintext
        Assert.assertEquals(plainText, decryptedData.decryptedText)
        Assert.assertNull(decryptedData.metaData)
    }

    @Test
    fun testEncryptionWithNegativeIterationCountFails() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val negativeIterationCount = -1000 // Negative iteration count

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Attempt to encrypt with a negative iteration count
        try {
            encryption.encrypt(passphrase, negativeIterationCount, "This is a test message.", null)
            // If encryption succeeded, the test should fail
            Assert.fail("Encryption with negative iteration count should fail.")
        } catch (e: Exception) {
            // If an exception is thrown, the test passes
        }
    }

    @Test
    fun testDecryptionWithIncorrectDataFails() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val iterationCount = 10000
        val incorrectCipherText = "IncorrectCipherText"

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Attempt to decrypt with incorrect data
        try {
            encryption.decrypt(passphrase, iterationCount, incorrectCipherText)
            // If decryption succeeded, the test should fail
            Assert.fail("Decryption with incorrect data should fail.")
        } catch (e: Exception) {
            // If an exception is thrown, the test passes
        }
    }
    @Test
    fun testDecryptionWithIncorrectIterationCountFails() {
        // Test data
        val passphrase = "StrongPassphrase123!"
        val correctIterationCount = 10000
        val incorrectIterationCount = 5000
        val plainText = "This is a test message."

        // Initialize the encryption class
        val encryption = AESEncryption3()

        // Encrypt the data with the correct iteration count
        val encryptedData = encryption.encrypt(passphrase, correctIterationCount, plainText, null)

        // Attempt to decrypt with the incorrect iteration count
        try {
            encryption.decrypt(passphrase, incorrectIterationCount, encryptedData)
            // If decryption succeeded, the test should fail
            Assert.fail("Decryption with incorrect iteration count should fail.")
        } catch (e: Exception) {
            // If an exception is thrown, the test passes
        }
    }
}