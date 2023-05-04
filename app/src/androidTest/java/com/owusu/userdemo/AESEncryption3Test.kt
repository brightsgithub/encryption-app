package com.owusu.userdemo

import android.util.Log
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AESEncryption3Test {

    val iterationCount = 10_000

    val SALT = "enter salt here"
    private val PASSPHRASE = "enter passphrase here"
    private val textToBeEncrypted = "\n\nthe test program\n\n"

    @Test
    fun testEncryptionDecryption() {

        //Log.v("AESEncryption3Test", "Text to be encrtpted is\n"+textToBeEncrypted)
        val aes = AESEncryption3()

        // Initialize the key and IV from the passphrase and salt
        aes.initKeyAndIvFromPassphrase(PASSPHRASE, SALT, iterationCount)

        // Encrypt the plain text
        val encrypted = aes.encrypt(textToBeEncrypted)
        Log.v("AESEncryption3Test", "Encrypted is\n"+encrypted)

        // Decrypt the cipher text
        val decrypted = aes.decrypt(encrypted)
        Log.v("AESEncryption3Test", "Decrypted is\n"+decrypted)

        // Check that the decrypted text matches the original plain text
        assertEquals(textToBeEncrypted, decrypted)
    }
}