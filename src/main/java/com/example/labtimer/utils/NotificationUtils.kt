package com.example.labtimer.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.labtimer.AppConstants
import com.example.labtimer.MainActivity
import com.example.labtimer.R
import com.example.labtimer.receivers.TimerNotificationActionReceiver

class NotificationUtils {
    companion object {
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "LabTimer Alarm"
        private const val TIMER_ID = 0


        @RequiresApi(Build.VERSION_CODES.O)
        fun showTimerExpired(context: Context){
            val stopIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            stopIntent.action = AppConstants.ACTION_STOP
            val startPendingIntent = PendingIntent.getBroadcast(
                context,
                0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val nBuilder = NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
            nBuilder.setContentTitle("LabTimer")
                .setContentText("Timer Expired")
                .setSmallIcon(R.drawable.notification_icon)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .addAction(R.drawable.stop_icon, "STOP", startPendingIntent)
                .setColor(context.getColor(R.color.colorPrimary))
                .setUsesChronometer(true)


            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

            nManager.notify(TIMER_ID, nBuilder.build())

    }

        fun hideTimerNotification(context: Context){
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
        }

        private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>): PendingIntent{
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun NotificationManager.createNotificationChannel(
            channelID: String,
            channelName: String,
            important: Boolean
        ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channelImportance = if (important) NotificationManager.IMPORTANCE_HIGH
                else NotificationManager.IMPORTANCE_LOW
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val nChannel = NotificationChannel(channelID, channelName, channelImportance)
                with(nChannel){
                    enableLights(true)
                    lightColor = Color.BLUE
                    if (important) {
                        enableVibration(true)
                        vibrationPattern = AppConstants.BUZZ_PATTERN_LONG
                        setSound(Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
                    }
                }
                this.createNotificationChannel(nChannel)
            }
        }
    }
}