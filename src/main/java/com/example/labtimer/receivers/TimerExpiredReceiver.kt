package com.example.labtimer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.util.Log
import com.example.labtimer.AppConstants
import com.example.labtimer.utils.AlarmUtils
import com.example.labtimer.utils.NotificationUtils
import com.example.labtimer.utils.TimerState
import com.example.labtimer.utils.TimerUtils

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(1000) // Vibrate method for below API Level 26
        }

        val caller = intent.getIntExtra(AppConstants.ID_EXTRA,0)
        Log.i("labtimer","Broadcast received from caller $caller")

        NotificationUtils.showTimerExpired(context,caller)

        if (caller == AppConstants.MAIN_ACTIVITY) {
            AlarmUtils.setAlarmTime(0, 0, context)
            TimerUtils.timerState.value = TimerState.Finished
        }
    }


}
