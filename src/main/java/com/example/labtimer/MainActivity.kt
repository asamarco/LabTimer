package com.example.labtimer

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.labtimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(TimerViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.timerViewModel = viewModel
        binding.setLifecycleOwner { this.lifecycle }

        viewModel.eventTimeFinished.observe(this, Observer {Done ->
            if (Done) {
                viewModel.eventTimeFinished.value=false
                buzz()
                viewModel.resetTimer()
            }
        })

        binding.secondButton.setOnLongClickListener {
            add10Seconds()
            true
        }
        binding.minuteButton.setOnLongClickListener {
            add10Minutes()
            true
        }

        binding.startButton.setOnLongClickListener {
            viewModel.clearTimer()
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
        if(viewModel.timerState == TimerState.Stopped) viewModel.startTimer()
        else viewModel.stopTimer()
    }


    private fun buzz() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(1000) // Vibrate method for below API Level 26
            }
        }
    }

}

