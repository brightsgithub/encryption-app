package com.owusu.userdemo

import android.util.Log
import junit.framework.TestCase
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val iterationCount = 10_000
    private val PASSPHRASE = "passphrase"
    private val textToBeEncrypted = "\n\nthe test program\n\n"
    @Test
    fun testEncryptionDecryption() {

        //Log.v("AESEncryption3Test", "Text to be encrtpted is\n"+textToBeEncrypted)
        val aes = AESEncryption3()

        // Encrypt the plain text
        val encrypted = aes.encrypt(PASSPHRASE, iterationCount, textToBeEncrypted, "some data")
        System.out.println("AESEncryption3Test Encrypted is\n"+encrypted)

        // Decrypt the cipher text
        val decrypted = aes.decrypt(PASSPHRASE, iterationCount, encrypted)
        System.out.println("AESEncryption3Test Decrypted is\n"+decrypted.decryptedText)

        //Check that the decrypted text matches the original plain text
        TestCase.assertEquals(textToBeEncrypted, decrypted.decryptedText)
    }
}