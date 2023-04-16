package com.owusu.userdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val encryptButton = findViewById<Button>(R.id.encryption_button)
        val hashingButton = findViewById<Button>(R.id.hashing_button)

        encryptButton.setOnClickListener {
            startActivity(Intent(this, EncryptActivity::class.java))
        }

        hashingButton.setOnClickListener {
            startActivity(Intent(this, HashTextActivity::class.java))
        }

    }
}


