package com.example.labtimer

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.labtimer.databinding.ActivityMainBinding

private val CORRECT_BUZZ_PATTERN = longArrayOf(500, 100, 100, 100, 100, 100)

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(TimerViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.timerViewModel = viewModel
        binding.setLifecycleOwner { this.lifecycle }
        window.decorView.rootView.isHapticFeedbackEnabled = true
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);




        viewModel.eventTimeFinished.observe(this, Observer {Done ->
            if (Done) {
                viewModel.eventTimeFinished.value=false
                buzz(CORRECT_BUZZ_PATTERN)
                viewModel.resetTimer()
            }
        })

        binding.secondButton.setOnLongClickListener {
            add10Seconds()
            binding.secondButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            true
        }
        binding.minuteButton.setOnLongClickListener {
            add10Minutes()
            binding.secondButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            true
        }

        binding.startButton.setOnLongClickListener {
            viewModel.clearTimer()
            binding.secondButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            true
        }


    }

    fun add10Minutes () {
        viewModel.addTime(600)
    }

    fun addMinute (view: View) {
        viewModel.addTime(60)
    }

    fun add10Seconds () {

        viewModel.addTime(10)
    }

    fun addSecond (view: View) {
        viewModel.addTime(1)
    }

    fun startStop (view: View) {
        if(viewModel.timerState == TimerState.Stopped) {
            viewModel.startTimer()
            binding.startButton.text = "STOP"
        }
        else {
            viewModel.stopTimer()
            binding.startButton.text = "START"
            if (this::vibrator.isInitialized) vibrator.cancel()

        }
    }


    private fun buzz(pattern: LongArray) {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern,0)) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(1000) // Vibrate method for below API Level 26
            }
        }
    }

}

