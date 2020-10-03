package com.example.labtimer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.labtimer.AppConstants
import com.example.labtimer.utils.AlarmUtils
import com.example.labtimer.utils.NotificationUtils
import com.example.labtimer.utils.TimerUtils

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AppConstants.ACTION_STOP -> {
                AlarmUtils.removeAlarm(context)
                TimerUtils.resetTimer()
                TimerUtils.stopTimer()
                NotificationUtils.hideTimerNotification(context)
            }

        }
    }
}