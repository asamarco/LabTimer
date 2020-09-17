package com.example.labtimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TimerExpiredReceiver : BroadcastReceiver() {
    private lateinit var viewModel: TimerViewModel
    override fun onReceive(context: Context, intent: Intent) {
        AlarmUtils.setAlarmTime(0, 0, context)
        Log.i("labtimer","Broadcast received")
        TimerUtils.timerState.value = TimerState.Finished

    }
}
