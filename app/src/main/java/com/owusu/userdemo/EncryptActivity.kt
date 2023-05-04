package com.owusu.userdemo

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class EncryptActivity : AppCompatActivity() {

    val aes = AESEncryption3()
    private lateinit var mProgressDialog: ProgressDialog
    lateinit var timerHandler : Handler
    lateinit var timeRunnable : Runnable
    var isInPreviewMode = true
    private lateinit var myDateUtils: MyDateUtils
    private lateinit var startTime: String
    private lateinit var endTime: String
    private lateinit var timeTakenView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myDateUtils = MyDateUtils.getInstance()
        setContentView(R.layout.activity_encrypt)
        initProgressDialog()
        val previewBtn = findViewById<Button>(R.id.preview_btn)
        val lockOpenImg = findViewById<ImageView>(R.id.lock_open)
        val lockedImg = findViewById<ImageView>(R.id.locked)
        val textEdit = findViewById<EditText>(R.id.text_edit)
        val saltEdit = findViewById<TextInputEditText>(R.id.salt_edit)
        val passphraseEdit = findViewById<TextInputEditText>(R.id.passphrase_edit)
        val iterationCountEdit = findViewById<TextInputEditText>(R.id.iteration_count_edit)
        val encryptButton = findViewById<Button>(R.id.encrypt_button)
        val decryptButton = findViewById<Button>(R.id.decrypt_button)
        val resultView = findViewById<TextView>(R.id.result_view)
        timeTakenView = findViewById(R.id.time_taken)

        previewBtn.setOnClickListener {
            isInPreviewMode = !isInPreviewMode
            val isEditable = isInPreviewMode
            textEdit.isEnabled = isEditable
            saltEdit.isEnabled = isEditable
            passphraseEdit.isEnabled = isEditable
            iterationCountEdit.isEnabled = isEditable
            //encryptButton.isEnabled = isEditable
            //decryptButton.isEnabled = isEditable
            if(!isEditable) {
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show()
                lockOpenImg.visibility = View.GONE
                lockedImg.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show()
                lockOpenImg.visibility = View.VISIBLE
                lockedImg.visibility = View.GONE
            }
        }

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
                        showTimeTaken()
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
                        showTimeTaken()
                    }
                }
            }
        }
    }

    private fun initProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Please wait...")
        mProgressDialog.setCancelable(false)

//        val startTime = System.currentTimeMillis()
//        timerHandler = Handler()
//        timeRunnable = object : Runnable {
//            override fun run() {
//                val currentTime = System.currentTimeMillis()
//                val elapsedTime = (currentTime - startTime) / 1000 // Convert to seconds
//                mProgressDialog.setMessage("$elapsedTime seconds elapsed")
//                timerHandler.postDelayed(this, 1000)
//            }
//        }
//        timerHandler.post(timeRunnable)
    }


    fun closeDialog() {
        mProgressDialog.cancel()
        endTime = getCurrentTime()
    }

    /**
     * Show progress dialog to user.
     */
    fun showDialog() {
        startTime = getCurrentTime()
        mProgressDialog.show()
    }

    fun showTimeTaken() {
        val time = "Started at: "+ startTime+ "\nEnded at: "+ endTime
        timeTakenView.text = time
    }

    private fun getCurrentTime(): String {
        return myDateUtils.convertDateToFormattedStringWithTime(Calendar.getInstance().timeInMillis)
    }
}
