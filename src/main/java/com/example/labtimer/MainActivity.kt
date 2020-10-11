package com.example.labtimer

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.labtimer.AppConstants.Companion.BUZZ_PATTERN
import com.example.labtimer.databinding.ActivityMainBinding
import com.example.labtimer.utils.AlarmUtils
import com.example.labtimer.utils.NotificationUtils
import com.example.labtimer.utils.TimerState
import com.example.labtimer.utils.TimerUtils


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var lastClickTime = 0L // to record the last click action of M/S buttons

        viewModel = ViewModelProvider(this).get(TimerViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.timerViewModel = viewModel
        binding.setLifecycleOwner { this.lifecycle }
        window.decorView.rootView.isHapticFeedbackEnabled = true //not sure if really needed


        TimerUtils.timerState.observe(this, Observer { state ->
            Log.i("labtimer", "State: $state")
            when (state) {
                TimerState.Finished -> {
                    binding.startButton.text = getString(R.string.stop)
                    buzz(BUZZ_PATTERN)
                    TimerUtils.resetTimer()
                    binding.progressBar.progress=100 //needed if the finished state is triggered from outside the app
                }
                TimerState.Stopped -> {
                    binding.startButton.text = getString(R.string.start)
                    binding.progressBar.progress = 0
                    if (this::vibrator.isInitialized) vibrator.cancel()
                }
                TimerState.Running -> {
                    binding.startButton.text = getString(R.string.stop)
                }
                else -> Log.i("labtimer","What the hell")
            }
        })

        //short clicks are handled with data binding in the activity xml file
        //long click of the timer display or of both M and S clear the timer

        binding.timerText.setOnLongClickListener {
            TimerUtils.clearTimer()
            binding.timerText.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            true
        }

        binding.minuteButton.setOnLongClickListener {
            val now = AlarmUtils.now()

            if (now - lastClickTime < AppConstants.DOUBLE_CLICK_TIME){
                TimerUtils.clearTimer()
                binding.minuteButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            }
            else viewModel.addTime(600)

            lastClickTime = now
            true
        }

        binding.secondButton.setOnLongClickListener {
            val now = AlarmUtils.now()

            if (now - lastClickTime < AppConstants.DOUBLE_CLICK_TIME){
                TimerUtils.clearTimer()
                binding.secondButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            }
            else viewModel.addTime(10)

            lastClickTime = now
            true
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

    override fun onResume() {
        super.onResume()
        val alarmSetTime = AlarmUtils.getAlarmTime(this)
        val timerLengthSaved = AlarmUtils.getAlarmLength(this)

        Log.i("labtimer", "timerLenghtSaved = $timerLengthSaved")
        AlarmUtils.removeAlarm( this)

        if (alarmSetTime > 0) {
            val remainingTime = (alarmSetTime - AlarmUtils.now()) / 1000
            TimerUtils.resumeTimer(remainingTime, timerLengthSaved)
        }
        if (TimerUtils.timerState.value == TimerState.Finished){
            NotificationUtils.hideTimerNotification(this)
            TimerUtils.resetTimer()
            TimerUtils.stopTimer()
        }

    }

    override fun onPause() {
        super.onPause()

        if (TimerUtils.timerState.value == TimerState.Running) {
            AlarmUtils.setAlarm(this, TimerUtils.secondsRemaining(), TimerUtils.timerLength)
            TimerUtils.timer.cancel()
            Log.i("labtimer","Paused, Alarm Set")
        }
    }

}

