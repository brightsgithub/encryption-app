package com.owusu.userdemo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import java.util.*

class CustomProgressLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val progressBar: ProgressBar
    private val timerTextView: TextView
    private var timer: Timer? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_progress_layout, this, true)
        progressBar = findViewById(R.id.progressBar)
        timerTextView = findViewById(R.id.timerTextView)
    }

    fun startTimer() {
        var seconds = 0
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                seconds++
                val minutes = seconds / 60
                val secondsRemainder = seconds % 60
                post {
                    timerTextView.text = "$minutes:${String.format("%02d", secondsRemainder)}"
                }
            }
        }, 0, 1000)
    }

    fun stopTimer() {
        timer?.cancel()
        timer = null
        timerTextView.text = "0:00"
    }
}
