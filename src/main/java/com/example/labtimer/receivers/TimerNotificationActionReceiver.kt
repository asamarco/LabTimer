package com.example.labtimer.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.labtimer.AppConstants
import com.example.labtimer.LabTimerWidget
import com.example.labtimer.WidgetTimer
import com.example.labtimer.WidgetTimer.Companion.timerMap
import com.example.labtimer.utils.AlarmUtils
import com.example.labtimer.utils.NotificationUtils
import com.example.labtimer.utils.TimerUtils

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val caller = intent.getIntExtra(AppConstants.ID_EXTRA, AppConstants.MAIN_ACTIVITY)
        when (intent.action) {
            AppConstants.ACTION_STOP -> {
                if (caller == AppConstants.MAIN_ACTIVITY){
                    AlarmUtils.removeAlarm(context)
                    TimerUtils.resetTimer()
                    TimerUtils.stopTimer()
                }
                else {
                    WidgetTimer.stopWidgetTimer(caller,timerMap[caller]!!.timerLength)
                    LabTimerWidget.updateAppWidget(context, AppWidgetManager.getInstance(context),caller)
                }
                NotificationUtils.hideTimerNotification(context,caller)
            }

        }
    }
}