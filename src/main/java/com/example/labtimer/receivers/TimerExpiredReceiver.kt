package com.example.labtimer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.labtimer.utils.AlarmUtils
import com.example.labtimer.utils.NotificationUtils
import com.example.labtimer.utils.TimerState
import com.example.labtimer.utils.TimerUtils

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtils.showTimerExpired(context)
        AlarmUtils.setAlarmTime(0, 0, context)
        Log.i("labtimer","Broadcast received")
        TimerUtils.timerState.value = TimerState.Finished
    }


}
