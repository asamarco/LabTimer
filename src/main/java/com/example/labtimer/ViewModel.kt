package com.example.labtimer

import android.text.format.DateUtils
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.labtimer.TimerUtils.Companion.timerState

const val ONE_SECOND = 1000L


class TimerViewModel : ViewModel() {



    val currentTimeString = Transformations.map (TimerUtils.currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    fun startStop() {
        when (timerState.value) {
            TimerState.Running -> TimerUtils.stopTimer()
            TimerState.Stopped -> TimerUtils.startTimer()
            TimerState.Finished -> TimerUtils.stopTimer()
        }
    }

    fun addTime (time: Long) : Boolean {
        TimerUtils.currentTime.value = TimerUtils.currentTime.value!!.plus(time)
        TimerUtils.stopTimer()
        return true //needed for onLongClick handling
    }


}