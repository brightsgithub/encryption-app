package com.owusu.userdemo

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EncryptActivity : AppCompatActivity() {

    val aes = AESEncryption3()
    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt)
        initProgressDialog()
        val textEdit = findViewById<TextInputEditText>(R.id.text_edit)
        val saltEdit = findViewById<TextInputEditText>(R.id.salt_edit)
        val passphraseEdit = findViewById<TextInputEditText>(R.id.passphrase_edit)
        val iterationCountEdit = findViewById<TextInputEditText>(R.id.iteration_count_edit)
        val encryptButton = findViewById<Button>(R.id.encrypt_button)
        val decryptButton = findViewById<Button>(R.id.decrypt_button)
        val resultView = findViewById<TextView>(R.id.result_view)



        encryptButton.setOnClickListener {
            val textToEncrypt = textEdit.text.toString()
            val salt = saltEdit.text.toString()
            val passphrase = passphraseEdit.text.toString()
            val iterationCount = iterationCountEdit.text.toString()

            if (textToEncrypt.isNotEmpty() && salt.isNotEmpty() && passphrase.isNotEmpty() && iterationCount.isNotEmpty()) {
                showDialog()
                GlobalScope.launch {

                    var encryptedText = "did not work"
                    withContext(Dispatchers.IO) {
                        aes.initKeyAndIvFromPassphrase(passphrase, salt, iterationCount.toInt())
                        encryptedText = aes.encrypt(textToEncrypt)
                    }

                    withContext(Dispatchers.Main) {
                        resultView.text = encryptedText
                        closeDialog()
                    }
                }
            }
        }

        decryptButton.setOnClickListener {
            val textToDecrypt = textEdit.text.toString()
            val salt = saltEdit.text.toString()
            val passphrase = passphraseEdit.text.toString()
            val iterationCount = iterationCountEdit.text.toString()

            if (textToDecrypt.isNotEmpty() && salt.isNotEmpty() && passphrase.isNotEmpty() && iterationCount.isNotEmpty()) {
                showDialog()

                GlobalScope.launch {

                    var decryptedText = ""

                    withContext(Dispatchers.IO) {
                        aes.initKeyAndIvFromPassphrase(passphrase, salt, iterationCount.toInt())
                        decryptedText = try {
                            aes.decrypt(textToDecrypt)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            e.stackTrace.toString()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        resultView.text = decryptedText
                        closeDialog()
                    }
                }
            }
        }
    }

    private fun initProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Please wait...")
        mProgressDialog.setCancelable(false)
    }

    fun closeDialog() {
        mProgressDialog.cancel()
    }

    /**
     * Show progress dialog to user.
     */
    fun showDialog() {
        //if (!mProgressDialog.isShowing) {
            mProgressDialog.show()
       // }
    }
}
