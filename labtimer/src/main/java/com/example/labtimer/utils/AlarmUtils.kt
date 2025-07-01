package com.example.labtimer.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.labtimer.AppConstants
import com.example.labtimer.receivers.TimerExpiredReceiver
import java.util.*
import androidx.core.content.edit


const val ONE_SECOND = 1000L

class AlarmUtils {
    companion object {

        // It's still good to have this as a separate check if needed elsewhere
        fun canScheduleExactAlarms(context: Context): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        }

        /**
         * Attempts to set an alarm.
         * If SCHEDULE_EXACT_ALARM permission is needed and not granted,
         * it will attempt to request it *IF* the provided context is an Activity.
         *
         * @param context The context. MUST be an Activity context if a permission request might be needed.
         * @return true if the alarm was set OR if a permission request was initiated.
         *         false if the alarm could not be set due to an immediate error (e.g., context not an Activity
         *         when permission is needed, or Settings activity not found).
         */
        fun setAlarm(context: Context, secondsRemaining: Long, timerLength: Long, caller: Int = 0): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w("AlarmUtils", "Missing SCHEDULE_EXACT_ALARM permission. Attempting to request.")
                    if (context is Activity) { // IMPORTANT: Check if context is an Activity
                        Intent().apply {
                            action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        }.also { intent ->
                            try {
                                context.startActivity(intent) // Consider startActivityForResult if you need to react
                                Log.i("AlarmUtils", "Redirecting to settings for SCHEDULE_EXACT_ALARM from setAlarm.")
                                return true // Indicate that a permission request was initiated
                            } catch (e: ActivityNotFoundException) {
                                Log.e("AlarmUtils", "Could not open settings for SCHEDULE_EXACT_ALARM from setAlarm", e)
                                return false // Indicate failure to open settings
                            }
                        }
                    } else {
                        Log.e("AlarmUtils", "Cannot request SCHEDULE_EXACT_ALARM: Context is not an Activity. Alarm not set.")
                        // Consider throwing an IllegalArgumentException here or clearly documenting this requirement.
                        return false // Indicate alarm not set because context was not an Activity
                    }
                }
            }

            val alarmTime = now() + secondsRemaining * ONE_SECOND
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            intent.putExtra(AppConstants.ID_EXTRA, caller)

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, caller, intent, pendingIntentFlags)

            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                Log.i("AlarmUtils", "Exact alarm set for caller $caller at $alarmTime")
                if (caller == AppConstants.MAIN_ACTIVITY) setAlarmTime(alarmTime, timerLength, context)
                return true // Alarm set successfully
            } catch (se: SecurityException) {
                Log.e("AlarmUtils", "SecurityException while setting exact alarm. This should ideally not happen if permission flow is correct.", se)
            } catch (e: Exception) {
                Log.e("AlarmUtils", "Exception while setting exact alarm.", e)
            }
            return false // Alarm not set due to an error
        }

        fun removeAlarm(context: Context, caller: Int = 0) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, caller, intent, pendingIntentFlags)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
            if (caller == AppConstants.MAIN_ACTIVITY) setAlarmTime(0, 0, context)
            Log.i("AlarmUtils", "Alarm removed for caller $caller")
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
            PreferenceManager.getDefaultSharedPreferences(context).edit() {
                putLong(ALARM_SET_TIME_ID, time)
                putLong(ALARM_SET_LENGTH_ID, length)
            }
        }

        fun now (): Long{
            return Calendar.getInstance().timeInMillis
        }
    }
}