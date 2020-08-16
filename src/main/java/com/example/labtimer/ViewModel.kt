package com.example.labtimer

import android.os.CountDownTimer
import android.text.format.DateUtils
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
        if (timerState == TimerState.Stopped) timerLenght += time
        else {
            stopTimer()
        }
    }

    val currentTimeString = Transformations.map (currentTime,{ time ->
        DateUtils.formatElapsedTime(time)
    })

    fun startTimer () {
        timer = object : CountDownTimer(timerLenght*1000, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                currentTime.value = currentTime.value?.minus(ONE_SECOND/1000)
            }

            override fun onFinish() {
                eventTimeFinished.value=true
                timerState = TimerState.Stopped
            }

        }

        timerState = TimerState.Running
        timer.start()
    }

    fun stopTimer () {
        timer.cancel()
        timerLenght = currentTime.value ?:0
        timerState = TimerState.Stopped
    }

    fun resetTimer () {
        currentTime.value = timerLenght
        timerState = TimerState.Stopped
        timer.cancel()
    }

    fun clearTimer () {
        timerLenght = 0
        resetTimer()
    }
    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}