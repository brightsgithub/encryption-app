package com.owusu.userdemo

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class EncryptActivity : AppCompatActivity() {

    private  lateinit var aes: AESEncryption3
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
        aes = AESEncryption3()
        myDateUtils = MyDateUtils.getInstance()
        setContentView(R.layout.activity_encrypt)
        initProgressDialog()
        val previewBtn = findViewById<Button>(R.id.preview_btn)
        val lockOpenImg = findViewById<ImageView>(R.id.lock_open)
        val lockedImg = findViewById<ImageView>(R.id.locked)
        val textEdit = findViewById<EditText>(R.id.text_edit)
        val metadataEdit = findViewById<TextInputEditText>(R.id.metadata_edit)
        val passphraseEdit = findViewById<TextInputEditText>(R.id.passphrase_edit)
        val iterationCountEdit = findViewById<TextInputEditText>(R.id.iteration_count_edit)
        val encryptButton = findViewById<Button>(R.id.encrypt_button)
        val decryptButton = findViewById<Button>(R.id.decrypt_button)
        val clearButton = findViewById<Button>(R.id.clear_button)
        val resultView = findViewById<TextView>(R.id.result_view)
        val qrDesc= findViewById<TextView>(R.id.qr_desc)
        val imageCode: ImageView = findViewById(R.id.imageCode)

        timeTakenView = findViewById(R.id.time_taken)

        previewBtn.setOnClickListener {
            isInPreviewMode = !isInPreviewMode
            val isEditable = isInPreviewMode
            textEdit.isEnabled = isEditable
            metadataEdit.isEnabled = isEditable
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
            val metadata = metadataEdit.text.toString()
            val passphrase = passphraseEdit.text.toString()
            val iterationCount = iterationCountEdit.text.toString()

            if (textToEncrypt.isNotEmpty() && passphrase.isNotEmpty() && iterationCount.isNotEmpty()) {
                showDialog()
                GlobalScope.launch {

                    var encryptedText = "did not work"
                    withContext(Dispatchers.IO) {
                        encryptedText = aes.encrypt(passphrase, iterationCount.toInt(), textToEncrypt, metadata)
                    }

                    withContext(Dispatchers.Main) {
                        resultView.text = encryptedText
                        closeDialog()
                        showTimeTaken()
                        showQRCode(imageCode, encryptedText)
                        qrDesc.text =  "Metadata: "+metadata
                    }
                }
            }
        }

        decryptButton.setOnClickListener {
            val textToDecrypt = textEdit.text.toString()
            val passphrase = passphraseEdit.text.toString()
            val iterationCount = iterationCountEdit.text.toString()

            if (textToDecrypt.isNotEmpty() && passphrase.isNotEmpty() && iterationCount.isNotEmpty()) {
                showDialog()

                GlobalScope.launch {

                    var decryptedWrapper = AESEncryption3.DecryptedWrapperData("Error","")

                    withContext(Dispatchers.IO) {
                        decryptedWrapper = try {
                            aes.decrypt(passphrase, iterationCount.toInt(), textToDecrypt)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            decryptedWrapper
                        }
                    }

                    withContext(Dispatchers.Main) {
                        resultView.text = decryptedWrapper.decryptedText
                        closeDialog()
                        showTimeTaken()
                        showQRCode(imageCode, decryptedWrapper.decryptedText)
                        qrDesc.text = "Metadata: "+decryptedWrapper.metaData
                    }
                }
            }
        }

        clearButton.setOnClickListener {
            aes = AESEncryption3()
            textEdit.setText("")
            passphraseEdit.setText("")
            metadataEdit.setText("")
            iterationCountEdit.setText("")
            timeTakenView.text = ""
            resultView.text = ""
            qrDesc.text = ""
            imageCode.setImageBitmap(null)
        }
    }

    /**
     * QR Code has a limited size of text it can take
     */
    private fun showQRCode(imageCode: ImageView, text: String) {
        //initializing MultiFormatWriter for QR code
        val mWriter = MultiFormatWriter()
        try {
            //BitMatrix class to encode entered text and set Width & Height
            val mMatrix: BitMatrix = mWriter.encode(text, BarcodeFormat.QR_CODE, 1200, 1200)
            val mEncoder = BarcodeEncoder()
            val mBitmap: Bitmap = mEncoder.createBitmap(mMatrix) //creating bitmap of code
            imageCode.setImageBitmap(mBitmap) //Setting generated QR code to imageView
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun initProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setMessage("Please wait...")
        mProgressDialog.setCancelable(false)
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
