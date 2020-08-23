package com.example.labtimer

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

const val ONE_SECOND = 1000L

enum class TimerState{
    Stopped, Paused, Running
}

class TimerViewModel : ViewModel() {

    lateinit var timer: CountDownTimer
    var currentTime = MutableLiveData<Long>()
    var eventTimeFinished = MutableLiveData<Boolean>()



    var timerState: TimerState = TimerState.Stopped
    private var timerLenght: Long = 0

    init {
        currentTime.value = 0
    }

    fun addTime (time: Long){
        currentTime.value = currentTime.value!!.plus(time)
        stopTimer()
    }

    val currentTimeString = Transformations.map (currentTime,{ time ->
        DateUtils.formatElapsedTime(time)
    })

    fun startTimer () {
        var tick = 1L

        if (timerLenght == 0L) {timerLenght= Long.MAX_VALUE/1000; tick = -1L} //Runup timer
        Log.i("labtimer", "timerLength = $timerLenght")

        timer = object : CountDownTimer(timerLenght*1000, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                currentTime.value = currentTime.value?.minus(tick)
                //Log.i("labtimer", "currenTime = $currentTime.value")
            }

            override fun onFinish() {
                eventTimeFinished.value=true
                //timerState = TimerState.Stopped
            }

        }

        timerState = TimerState.Running
        timer.start()
    }

    fun stopTimer () {
        if (this::timer.isInitialized) timer.cancel()
        timerLenght = currentTime.value ?:0
        timerState = TimerState.Stopped
    }

    fun resetTimer () {
        currentTime.value = timerLenght
        //timerState = TimerState.Stopped
        if (this::timer.isInitialized) timer.cancel()
    }

    fun clearTimer () {
        timerLenght = 0
        timerState = TimerState.Stopped
        resetTimer()
    }
    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}