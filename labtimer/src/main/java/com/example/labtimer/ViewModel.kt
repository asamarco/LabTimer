package com.example.labtimer

import android.text.format.DateUtils
import androidx.lifecycle.map
import androidx.lifecycle.ViewModel
import com.example.labtimer.utils.TimerState
import com.example.labtimer.utils.TimerUtils
import com.example.labtimer.utils.TimerUtils.Companion.timerState



class TimerViewModel : ViewModel() {



    val currentTimeString = TimerUtils.currentTime.map { time ->
        DateUtils.formatElapsedTime(time)
    }

    val progress = TimerUtils.progress

    fun startStop() {
        when (timerState.value) {
            TimerState.Running -> TimerUtils.stopTimer()
            TimerState.Stopped -> TimerUtils.startTimer()
            TimerState.Finished -> TimerUtils.stopTimer()
            null -> TODO()
        }
    }

    fun addTime (time: Long) : Boolean {
        TimerUtils.currentTime.value = TimerUtils.currentTime.value!!.plus(time)
        TimerUtils.stopTimer()
        return true //needed for onLongClick handling
    }


}