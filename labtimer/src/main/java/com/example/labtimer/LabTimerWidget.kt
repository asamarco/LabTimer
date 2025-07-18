package com.example.labtimer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.labtimer.WidgetTimer.Companion.addTime
import com.example.labtimer.WidgetTimer.Companion.clearWidgetTimer
import com.example.labtimer.WidgetTimer.Companion.defaultTimer
import com.example.labtimer.WidgetTimer.Companion.lastClickTime
import com.example.labtimer.WidgetTimer.Companion.stopWidgetTimer
import com.example.labtimer.WidgetTimer.Companion.timerMap
import com.example.labtimer.utils.AlarmUtils
import com.example.labtimer.utils.NotificationUtils

/**
 * Implementation of App Widget functionality.
 */



class LabTimerWidget : AppWidgetProvider() {
    companion object {
    //Define the intent by the unique identifier of ID*value, each button/widgetID combination should use separate broadcast channels
    private fun getPendingIntent(context: Context, widgetId: Int, value: Int, action: String): PendingIntent {
        val intent = Intent(context, LabTimerWidget::class.java).apply {
            this.action = action
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId) // Standard extra for widget ID
            if (action == AppConstants.ADD_TIME_INTENT) {
                putExtra(AppConstants.TIME_EXTRA, value)
            }
            // For ACTION_WIDGET_TOGGLE_TIMER, 'value' might not be strictly needed from here,
            // as the action itself is specific.
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Ensure unique request codes for different actions or values if necessary.
        // A combination of widgetId, action hash, and value can ensure uniqueness.
        val requestCode = widgetId * 1000 + action.hashCode() + value
        return PendingIntent.getBroadcast(context, requestCode, intent, pendingIntentFlags)
    }

    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        if (timerMap[appWidgetId] == null) timerMap[appWidgetId] = defaultTimer
        // Construct the RemoteViews object
        val views: RemoteViews
        val isCountDownTimer = timerMap[appWidgetId]!!.timerLength > 500L //check with == 0 would produce counterintuitive behavior if 00:00 on screen hides something != 0 behind
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)


        val minWidth= options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        //val maxWidth= options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        //val minHeigth= options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxHeigth= options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        //Define the view according to the widget size on screen
        if (maxHeigth>100) {
            if (minWidth > 200) {
                views = RemoteViews(context.packageName, R.layout.lab_timer_widget_large)
            }
            else {
                views = RemoteViews(context.packageName, R.layout.lab_timer_widget)
                views.setTextViewTextSize(R.id.widgetTimerText, TypedValue.COMPLEX_UNIT_SP, 56F)
            }
        }
        else {
            views = RemoteViews(context.packageName, R.layout.lab_timer_widget)
            views.setFloat(R.id.widgetTimerText, "setTextSize", 28F)
        }
        // The rounding to seconds create some artifacts, offset time provides a workaround. 00:00 still remains for two seconds on display in a countdown timer
        val timeTarget: Long = if(isCountDownTimer)
            SystemClock.elapsedRealtime() + AppConstants.OFFSET_TIME + (timerMap[appWidgetId]!!.timerLength)
        else
            SystemClock.elapsedRealtime() - AppConstants.OFFSET_TIME + (timerMap[appWidgetId]!!.timerLength)

        timerMap[appWidgetId]!!.target = timeTarget

        if (timerMap[appWidgetId]!!.running)
            views.setTextViewText(R.id.widget_start_button,context.getString(R.string.stop))
        else
            views.setTextViewText(R.id.widget_start_button,context.getString(R.string.start))

        with(views){
            setChronometerCountDown(R.id.widgetTimerText, isCountDownTimer)

            setChronometer(R.id.widgetTimerText, timeTarget,null,timerMap[appWidgetId]!!.running)
            Log.i("intent","SET ID = $appWidgetId")
            setOnClickPendingIntent(R.id.widget_minute_button, getPendingIntent(context, appWidgetId, AppConstants.MINUTE, AppConstants.ADD_TIME_INTENT))
            setOnClickPendingIntent(R.id.widget_second_button, getPendingIntent(context, appWidgetId, AppConstants.SECOND, AppConstants.ADD_TIME_INTENT))
            setOnClickPendingIntent(R.id.widget_start_button, getPendingIntent(context, appWidgetId, AppConstants.START, AppConstants.ADD_TIME_INTENT))
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.i("intent","WIDGET ID = $appWidgetId")
    }
}
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            Log.i("intent","Update $appWidgetId in ${appWidgetIds.size}")
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions!!)
        //Just stops the timer and update the view
        stopWidgetTimer(appWidgetId)
        updateAppWidget(context!!,appWidgetManager!!,appWidgetId)
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        super.onReceive(context, intent)

        if (intent?.action == AppConstants.ADD_TIME_INTENT) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val time = intent.getIntExtra(AppConstants.TIME_EXTRA, 0)
            Log.i("intent","Id $appWidgetId, time $time")

            if (timerMap[appWidgetId] == null) timerMap[appWidgetId] = defaultTimer
            val timerExpired = (timerMap[appWidgetId]!!.target < SystemClock.elapsedRealtime())

            //Whatever it has been pressed, if the time is zero or negative restores original timer setpoint and removes notification if still there
            if (timerExpired and timerMap[appWidgetId]!!.running)
            {
                stopWidgetTimer(appWidgetId, timerMap[appWidgetId]!!.timerLength)
                timerMap[appWidgetId]!!.running = false
                updateAppWidget(context!!,AppWidgetManager.getInstance(context),appWidgetId)
                NotificationUtils.hideTimerNotification(context,appWidgetId)
                return
            }

            if (time == AppConstants.START){
                if (timerMap[appWidgetId]!!.running) {
                    stopWidgetTimer(appWidgetId)
                    AlarmUtils.removeAlarm(context!!, appWidgetId)
                    timerMap[appWidgetId]!!.running = false
                }
                else {
                    timerMap[appWidgetId]!!.running = true
                    if (timerMap[appWidgetId]!!.timerLength != 0L) {
                        val secondsRemaining = timerMap[appWidgetId]!!.timerLength / 1000 //timerMap[appWidgetId]!!.target - SystemClock.elapsedRealtime()
                        AlarmUtils.setAlarm(
                            context!!,
                            secondsRemaining,
                            timerMap[appWidgetId]!!.timerLength,
                            appWidgetId
                        )
                    }
                }
            }
            else {
                AlarmUtils.removeAlarm(context!!, appWidgetId)

                val now = AlarmUtils.now()
                if (now - lastClickTime < AppConstants.DOUBLE_CLICK_TIME)
                    clearWidgetTimer(appWidgetId)
                else {
                    addTime(appWidgetId, time)
                }
                lastClickTime = now
            }

            updateAppWidget(context!!,AppWidgetManager.getInstance(context),appWidgetId)

        }
    }
}






