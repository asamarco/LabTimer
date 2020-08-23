package com.example.labtimer

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
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

        viewModel.timerState.observe(this, Observer { state ->
            Log.i("labtimer", "State: $state")
            when (state) {
                TimerState.Finished -> {
                    buzz(CORRECT_BUZZ_PATTERN)
                    viewModel.resetTimer()
                }
                TimerState.Stopped -> {
                    binding.startButton.text = getString(R.string.start)
                    if (this::vibrator.isInitialized) vibrator.cancel()
                }
                TimerState.Running -> {
                    binding.startButton.text = getString(R.string.stop)
                }
                else -> Log.i("labtimer","What the hell")
            }
        })


        binding.startButton.setOnLongClickListener {
            viewModel.clearTimer()
            binding.secondButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            true
        }


    }

    fun startStop(view: View) {
        when (viewModel.timerState.value) {
            TimerState.Running -> viewModel.stopTimer()
            TimerState.Stopped -> viewModel.startTimer()
            TimerState.Finished -> viewModel.stopTimer()
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

