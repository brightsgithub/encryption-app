package com.owusu.userdemo

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class HashTextActivity : AppCompatActivity() {

    private lateinit var hashUtils: HashUtils
    //private val SALT = "some salt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hash_text)

        hashUtils = HashUtils()

        val textEdit = findViewById<TextInputEditText>(R.id.text_to_hash_edit)
        val saltEdit = findViewById<TextInputEditText>(R.id.salt_edit)
        val hashButton = findViewById<Button>(R.id.hash_button)
        val compareButton = findViewById<Button>(R.id.compare_button)
        val resultView = findViewById<TextView>(R.id.hashed_text_view)
        val compareEditText = findViewById<TextView>(R.id.enter_input_to_match_hash_edit_text)
        val compareResult = findViewById<TextView>(R.id.compare_result)



        hashButton.setOnClickListener {
            setCompareResult(compareResult, "", Color.BLUE)
            compareEditText.text = ""

            val plainText = textEdit.text.toString()

            if (plainText.isNotEmpty()) {
                val salt = generateSalt(saltEdit)
                val hashedText = hashUtils.hashTextWithSalt(plainText, salt)
                resultView.text = hashedText
                //save salt securely
            }
        }

        compareButton.setOnClickListener {
            val SALT = saltEdit.text.toString()
            val compareText = compareEditText.text.toString()

            if (compareText.isNotEmpty() && textEdit.text.toString().isNotEmpty()) {
                val salt = generateSalt(saltEdit)
                val hashedText = hashUtils.hashTextWithSalt(compareText, salt)
                if (resultView.text.toString().equals(hashedText)) {
                    setCompareResult(compareResult, "TRUE", Color.GREEN)
                } else {
                    setCompareResult(compareResult, "FALSE", Color.RED)
                }
                //save salt securely
            }
        }
    }

    private fun setCompareResult(textView: TextView, text: String, color: Int) {
        textView.setTextColor(color)
        textView.text = text
    }

    private fun generateSalt(saltEdit: EditText) :ByteArray {
        val saltInput = saltEdit.text.toString()
        return  hashUtils.generateSalt(saltInput)
    }
}
