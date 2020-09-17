package com.example.labtimer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import java.util.*


const val ONE_SECOND = 1000L


class AlarmUtils {
    companion object{
        fun setAlarm(context: Context, secondsRemaining: Long, timerLength: Long){

            val alarmTime = now() + secondsRemaining*1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0)

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            setAlarmTime(alarmTime, timerLength, context)
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
            setAlarmTime(0,0,context)
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