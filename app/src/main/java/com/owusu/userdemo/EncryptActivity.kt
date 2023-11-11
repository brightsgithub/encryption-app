package com.owusu.userdemo

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
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

/**
 * adb -s 16081FDD4002TK uninstall com.owusu.userdemo; adb -s 52033501faab14e1 uninstall com.owusu.userdemo; adb -s emulator-5556 uninstall com.owusu.userdemo
 */

class EncryptActivity : AppCompatActivity() {

    private var _data: String? = ""
    private var _metadata: String? = ""
    private  lateinit var aes: AESEncryption3
    private lateinit var mProgressDialog: ProgressDialog
    lateinit var timerHandler : Handler
    lateinit var timeRunnable : Runnable
    var isInPreviewMode = true
    private lateinit var myDateUtils: MyDateUtils
    private lateinit var startTime: String
    private lateinit var endTime: String
    private lateinit var timeTakenView: TextView
    private lateinit var displayQRCodeSwitch: Button
    private lateinit var mainContainer: View

    private lateinit var passphraseEdit: TextInputEditText
    private lateinit var passphraseEditConfirm: TextInputEditText
    private lateinit var iterationCountEdit: TextInputEditText
    private lateinit var iterationCountEditConfirm: TextInputEditText
    private lateinit var textEdit: EditText
    private lateinit var metadataEdit: EditText
    private lateinit var resultView: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        aes = AESEncryption3()
        // Initialize MediaPlayer with the MP3 file
        mediaPlayer = MediaPlayer.create(this, R.raw.bell);
        myDateUtils = MyDateUtils.getInstance()
        setContentView(R.layout.activity_encrypt)
        initProgressDialog()
        displayQRCodeSwitch = findViewById(R.id.showQRPopUp)

        val lockOpenImg = findViewById<ImageView>(R.id.lock_open)
        val lockedImg = findViewById<ImageView>(R.id.locked)
        val lockIconContainer = findViewById<View>(R.id.lockIconContainer)
        textEdit = findViewById(R.id.text_edit)
        metadataEdit = findViewById<TextInputEditText>(R.id.metadata_edit)
        passphraseEdit = findViewById(R.id.passphrase_edit)
        passphraseEditConfirm = findViewById(R.id.passphrase_edit_confirm)
        iterationCountEdit = findViewById(R.id.iteration_count_edit)
        iterationCountEditConfirm = findViewById(R.id.iteration_count_edit_confirm)
        val encryptButton = findViewById<Button>(R.id.encrypt_button)
        val decryptButton = findViewById<Button>(R.id.decrypt_button)
        val clearButton = findViewById<Button>(R.id.clear_button)
        resultView = findViewById(R.id.result_view)
        val qrDesc= findViewById<TextView>(R.id.qr_desc)
        val imageCode: ImageView = findViewById(R.id.imageCode)

        timeTakenView = findViewById(R.id.time_taken)
        mainContainer = findViewById(R.id.mainContainer)

        lockIconContainer.setOnClickListener {
            isInPreviewMode = !isInPreviewMode
            val isEditable = isInPreviewMode
            textEdit.isEnabled = isEditable
            metadataEdit.isEnabled = isEditable
            passphraseEdit.isEnabled = isEditable
            passphraseEditConfirm.isEnabled = isEditable
            iterationCountEdit.isEnabled = isEditable
            iterationCountEditConfirm.isEnabled = isEditable
            encryptButton.isEnabled = isEditable
            decryptButton.isEnabled = isEditable
            clearButton.isEnabled = isEditable
            if(!isEditable) {
                hideSensitiveInfo()
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show()
                lockOpenImg.visibility = View.GONE
                lockedImg.visibility = View.VISIBLE
            } else {
                showSensitiveInfo()
                Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show()
                lockOpenImg.visibility = View.VISIBLE
                lockedImg.visibility = View.GONE
            }
        }

        encryptButton.setOnClickListener {
            val textToEncrypt = textEdit.text.toString()
            val metadata = metadataEdit.text.toString()
            val passphrase = passphraseEdit.text.toString()
            val passphraseConfirm = passphraseEditConfirm.text.toString()
            val iterationCount = iterationCountEdit.text.toString()
            val iterationCountConfirm = iterationCountEditConfirm.text.toString()

            val doesPassPhraseMatch = doFieldsMatch(passphrase, passphraseConfirm, "Passphrase")
            val doesIterationCountMatch = doFieldsMatch(iterationCount, iterationCountConfirm, "iteration count")
            val areAllFieldsValid = doesPassPhraseMatch && doesIterationCountMatch

            if (areAllFieldsValid) {
                if (textToEncrypt.isNotEmpty() && passphrase.isNotEmpty() && iterationCount.isNotEmpty()) {
                    showDialog()
                    lockIconContainer.performClick()
                    GlobalScope.launch {

                        var encryptedText = "did not work"
                        withContext(Dispatchers.IO) {
                            encryptedText = aes.encrypt(passphrase, iterationCount.toInt(), textToEncrypt, metadata)
                        }

                        withContext(Dispatchers.Main) {
                            resultView.text = encryptedText
                            closeDialog()
                            playSound()
                            showTimeTaken()
                            qrDesc.text =  metadata
                            _data = encryptedText
                            _metadata = metadata
                        }
                    }
                }
            }
        }

        decryptButton.setOnClickListener {
            val textToDecrypt = textEdit.text.toString()
            val passphrase = passphraseEdit.text.toString()
            val passphraseConfirm = passphraseEditConfirm.text.toString()
            val iterationCount = iterationCountEdit.text.toString()
            val iterationCountConfirm = iterationCountEditConfirm.text.toString()

            val doesPassPhraseMatch = doFieldsMatch(passphrase, passphraseConfirm, "Passphrase")
            val doesIterationCountMatch = doFieldsMatch(iterationCount, iterationCountConfirm, "iteration count")
            val areAllFieldsValid = doesPassPhraseMatch && doesIterationCountMatch

            if (areAllFieldsValid) {
                if (textToDecrypt.isNotEmpty() && passphrase.isNotEmpty() && iterationCount.isNotEmpty()) {
                    showDialog()
                    lockIconContainer.performClick()
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
                            playSound()
                            showTimeTaken()
                            qrDesc.text = decryptedWrapper.metaData
                            _data = decryptedWrapper.decryptedText
                            _metadata = decryptedWrapper.metaData
                        }
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
            _data = null
            _metadata = null
        }

        displayQRCodeSwitch.setOnClickListener {
            showQRCodePopup(_data, _metadata)
        }
    }

    private fun playSound() {
        mediaPlayer?.start();
    }

    private fun hideSensitiveInfo() {
        resultView.visibility = View.INVISIBLE
        metadataEdit.visibility = View.INVISIBLE
        textEdit.visibility = View.INVISIBLE
        passphraseEdit.visibility = View.INVISIBLE
        passphraseEditConfirm.visibility = View.INVISIBLE
        iterationCountEdit.visibility = View.INVISIBLE
        iterationCountEditConfirm.visibility = View.INVISIBLE
    }

    private fun showSensitiveInfo() {
        resultView.visibility = View.VISIBLE
        metadataEdit.visibility = View.VISIBLE
        textEdit.visibility = View.VISIBLE
        passphraseEdit.visibility = View.VISIBLE
        passphraseEditConfirm.visibility = View.VISIBLE
        iterationCountEdit.visibility = View.VISIBLE
        iterationCountEditConfirm.visibility = View.VISIBLE
    }

    private fun doFieldsMatch(text1: String?, text2: String, fieldName: String): Boolean {
        if(text1 == null || text2 == null) {
            Toast.makeText(this, fieldName+ " Cannot be null", Toast.LENGTH_SHORT).show()
            return false
        }

        return if(text1 == text2) {
            true
        } else {
            Toast.makeText(this, fieldName+ " must match! ", Toast.LENGTH_SHORT).show()
            false
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
            val mMatrix: BitMatrix = mWriter.encode(text, BarcodeFormat.QR_CODE, 800, 800)
            val mEncoder = BarcodeEncoder()
            val mBitmap: Bitmap = mEncoder.createBitmap(mMatrix) //creating bitmap of code
            imageCode.setImageBitmap(mBitmap) //Setting generated QR code to imageView
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
    private fun showQRCodePopup(text: String?, metadata: String?) {
        if (text == null) return

        try {
            //val dialog = Dialog(this)
            // .Theme_Translucent_NoTitleBar, which provides a translucent background without any title bar or shadow.
            // This should give the appearance of a regular layout without dialog features.
            val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)

            dialog.setContentView(R.layout.qr_code_popup)
            dialog.setCancelable(false)
            val qrImageView = dialog.findViewById<ImageView>(R.id.qrCodeImageView)
            val qrDescTextView = dialog.findViewById<TextView>(R.id.qrDescTextView)
            val closeButton = dialog.findViewById<MaterialButton>(R.id.closeButton)

            qrImageView.setOnClickListener {
                if (closeButton.isVisible) {
                    closeButton.visibility = View.GONE
                } else {
                    closeButton.visibility = View.VISIBLE
                }
            }

            showQRCode(qrImageView, text)

            qrDescTextView.text = metadata

            // removes the dark gradient behind
            val window: Window? = dialog.window
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            // Set a percentage of screen width for the dialog (adjust as needed)
            val screenWidth = resources.displayMetrics.widthPixels
            val dialogWidth = (screenWidth * 0.8).toInt()
            dialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

            closeButton.setOnClickListener {
                dialog.dismiss()
                mainContainer.visibility = View.VISIBLE
                supportActionBar?.show()
            }

            dialog.show()
            Toast.makeText(this, "Tap QR to show/hide Close button", Toast.LENGTH_SHORT).show()
            supportActionBar?.hide()
            mainContainer.visibility = View.INVISIBLE
        } catch (ex: java.lang.Exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
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
        releaseWakeLock()
    }

    /**
     * Show progress dialog to user.
     */
    fun showDialog() {
        acquireWakeLock()
        startTime = getCurrentTime()
        mProgressDialog.show()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "com.owusu.userdemo:WakeLockTag"
            )
            wakeLock?.acquire()
            Toast.makeText(this, "Using WakeLock", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "acquireWakeLock Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.release()
            Toast.makeText(this, "WakeLock released", Toast.LENGTH_SHORT).show()
        }catch (e: java.lang.Exception) {
            Toast.makeText(this, "releaseWakeLock Error", Toast.LENGTH_SHORT).show()
        }
    }

    fun showTimeTaken() {
        val time = "Started at: "+ startTime+ "\nEnded at: "+ endTime
        timeTakenView.text = time
    }

    private fun getCurrentTime(): String {
        return myDateUtils.convertDateToFormattedStringWithTime(Calendar.getInstance().timeInMillis)
    }

    override fun onBackPressed() {
        // Do nothing.
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
}
