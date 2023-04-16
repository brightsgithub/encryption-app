package com.owusu.userdemo

import android.os.Build
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

class Encryption {
    companion object {
        fun encrypt(textToEncrypt: String, key: String): String {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val encryptedTextBytes = cipher.doFinal(textToEncrypt.toByteArray())
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(encryptedTextBytes)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }

        fun decrypt(textToDecrypt: String, key: String): String {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decryptedTextBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cipher.doFinal(Base64.getDecoder().decode(textToDecrypt))
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            return String(decryptedTextBytes)
        }
    }
}
