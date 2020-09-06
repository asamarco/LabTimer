package com.example.labtimer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import java.util.*

class AlarmUtils {
    companion object{



        fun setAlarm(context: Context, secondsRemaining: Long){

            val alarmTime = now() + secondsRemaining*1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0)

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            setAlarmTime(alarmTime, context)
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
            setAlarmTime(0,context)
        }

        private const val ALARM_SET_TIME_ID = "com.example.labtimer.background_timer"

        fun getAlarmTime(context: Context): Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return  preferences.getLong(ALARM_SET_TIME_ID, 0)
        }

        fun setAlarmTime(time: Long, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()
        }

        fun now (): Long{
            return Calendar.getInstance().timeInMillis
        }

    }


}