package com.example.labtimer.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.example.labtimer.AppConstants
import com.example.labtimer.receivers.TimerExpiredReceiver
import java.util.*


const val ONE_SECOND = 1000L

//alarms are identified by the caller index, of value MAIN_ACTIVITY for the app and corresponding to appWidgetId for appwidgets
class AlarmUtils {
    companion object{
        fun setAlarm(context: Context, secondsRemaining: Long, timerLength: Long, caller: Int = 0){

            val alarmTime = now() + secondsRemaining*1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            intent.putExtra(AppConstants.ID_EXTRA, caller)
            val pendingIntent = PendingIntent.getBroadcast(context,caller, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            if (caller == AppConstants.MAIN_ACTIVITY) setAlarmTime(alarmTime, timerLength, context)
        }

        fun removeAlarm(context: Context, caller: Int = 0){
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, caller, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
            if (caller == AppConstants.MAIN_ACTIVITY) setAlarmTime(0,0,context)
        }

        private const val ALARM_SET_TIME_ID = "com.example.labtimer.background_timer"
        private const val ALARM_SET_LENGTH_ID = "com.example.labtimer.background_timer_length"

        fun getAlarmTime(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return  preferences.getLong(ALARM_SET_TIME_ID, 0)
        }

        fun getAlarmLength(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return  preferences.getLong(ALARM_SET_LENGTH_ID, 0)
        }


        fun setAlarmTime(time: Long, length: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.putLong(ALARM_SET_LENGTH_ID, length)
            editor.apply()
        }

        fun now (): Long{
            return Calendar.getInstance().timeInMillis
        }
    }

}