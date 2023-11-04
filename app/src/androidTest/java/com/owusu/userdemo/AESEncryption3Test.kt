package com.owusu.userdemo

import android.util.Log
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AESEncryption3Test {

    val iterationCount = 10_000
    private val PASSPHRASE = "passphrase"
    private val textToBeEncrypted = "\n\nthe test program\n\n"

    @Test
    fun testEncryptionDecryption() {

        //Log.v("AESEncryption3Test", "Text to be encrtpted is\n"+textToBeEncrypted)
        val aes = AESEncryption3()

        // Encrypt the plain text
        val encrypted = aes.encrypt(PASSPHRASE, iterationCount, textToBeEncrypted, "some data")
        Log.v("AESEncryption3Test", "Encrypted is\n"+encrypted)

        // Decrypt the cipher text
        val decrypted = aes.decrypt(PASSPHRASE, iterationCount, encrypted)
        Log.v("AESEncryption3Test", "Decrypted is\n"+decrypted)

        // Check that the decrypted text matches the original plain text
        assertEquals(textToBeEncrypted, decrypted.decryptedText)
    }
}