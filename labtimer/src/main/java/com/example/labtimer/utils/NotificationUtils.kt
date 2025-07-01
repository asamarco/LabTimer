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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.labtimer.AppConstants
import com.example.labtimer.MainActivity
import com.example.labtimer.R
import com.example.labtimer.receivers.TimerNotificationActionReceiver

class NotificationUtils {
    companion object {
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "LabTimer Alarm"
        private const val TAG = "NotificationUtils" // For logging

        fun showTimerExpired(context: Context, caller: Int) {
            Log.d(TAG, "showTimerExpired called for caller: $caller")

            val stopIntent = Intent(context, TimerNotificationActionReceiver::class.java).apply {
                action = AppConstants.ACTION_STOP
                putExtra(AppConstants.ID_EXTRA, caller)
            }

            val stopPendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                caller, // requestCode for the stop action
                stopIntent,
                stopPendingIntentFlags
            )
            Log.d(TAG, "Stop PendingIntent created with flags: $stopPendingIntentFlags")


            val nBuilder = NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
                .setContentTitle("LabTimer")
                .setContentText("Timer Expired")
                .setSmallIcon(R.drawable.notification_icon)
                .setAutoCancel(true)
                .setOngoing(false)
                .addAction(R.drawable.stop_icon, "STOP", stopPendingIntent)
                .setColor(context.getColor(R.color.colorPrimary))
                .setUsesChronometer(true)

            if (caller == AppConstants.MAIN_ACTIVITY) {
                Log.d(TAG, "Configuring content intent for MainActivity")
                // Create flags for the content intent (tapping the notification body)
                val contentPendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Using a unique requestCode for content intent to avoid collision if 'caller' is 0
                    // And ensuring it's different from the stopPendingIntent's requestCode if 'caller' can be 0.
                    // FLAG_IMMUTABLE is generally safe for launching an Activity.
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                Log.d(TAG, "Content PendingIntent flags: $contentPendingIntentFlags")

                try {
                    val contentPendingIntent = getPendingIntentWithStack(
                        context,
                        MainActivity::class.java,
                        caller + 1000, // Ensuring a different request code for content intent than action
                        contentPendingIntentFlags
                    )
                    nBuilder.setContentIntent(contentPendingIntent)
                    Log.d(TAG, "Successfully set content intent")
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating/setting content PendingIntent: ${e.message}", e)
                    // Fallback or skip setting content intent if critical
                }
            } else {
                Log.d(TAG, "Not MAIN_ACTIVITY caller, no specific content intent set beyond default.")
            }

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannelIfNeeded(nManager) // Changed to avoid repeated creation logic inline

            try {
                nManager.notify(caller, nBuilder.build())
                Log.i(TAG, "Notification shown for caller: $caller")
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying manager: ${e.message}", e)
            }
        }

        fun hideTimerNotification(context: Context, caller: Int) {
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(caller)
            Log.i(TAG, "Notification hidden for caller: $caller")
        }

        private fun <T> getPendingIntentWithStack(
            context: Context,
            javaClass: Class<T>,
            requestCode: Int, // Added requestCode parameter
            flags: Int
        ): PendingIntent {
            val resultIntent = Intent(context, javaClass).apply {
                // Ensure flags here don't conflict with PendingIntent needs
                this.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            // TaskStackBuilder is used to create a synthetic back stack
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass) // Adds the parent stack from manifest
            stackBuilder.addNextIntent(resultIntent) // Adds the intent that starts the activity

            // Use the provided flags and requestCode
            // Note: The first parameter to getPendingIntent is a requestCode for this PendingIntent.
            // It should be unique if you want different PendingIntents when the Intent data is the same.
            return stackBuilder.getPendingIntent(requestCode, flags)
                ?: throw IllegalStateException("getPendingIntentWithStack returned null") // Should not happen
        }

        // Helper to create channel only if needed
        private fun createNotificationChannelIfNeeded(notificationManager: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(CHANNEL_ID_TIMER) == null) {
                    Log.d(TAG, "Creating notification channel: $CHANNEL_ID_TIMER")
                    val channelImportance = NotificationManager.IMPORTANCE_HIGH
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()

                    val nChannel = NotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, channelImportance).apply {
                        enableLights(true)
                        lightColor = Color.BLUE
                        enableVibration(true)
                        vibrationPattern = AppConstants.BUZZ_PATTERN_LONG // Ensure this is defined
                        setSound(Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
                    }
                    notificationManager.createNotificationChannel(nChannel)
                } else {
                    Log.d(TAG, "Notification channel $CHANNEL_ID_TIMER already exists.")
                }
            }
        }
    }
}