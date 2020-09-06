package com.example.labtimer

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData

enum class TimerState{
    Stopped, Finished, Running
}
class TimerUtils {
    companion object{
        val UPPER_TIME_LIMIT = 604800L //1 week

        var currentTime = MutableLiveData<Long>() //seconds
        var timerState = MutableLiveData<TimerState>()
        lateinit var timer: CountDownTimer
        private var timerLenght: Long = 0

        init {
            currentTime.value = 0
            timerState.value = TimerState.Stopped
        }



        fun startTimer () {
            var tick = 1L

            if (timerLenght == 0L) {
                timerLenght= UPPER_TIME_LIMIT; tick = -1L} //Runup timer
            Log.i("labtimer", "timerLength = $timerLenght")

            timer = object : CountDownTimer(timerLenght*1000, ONE_SECOND) {

                override fun onTick(millisUntilFinished: Long) {
                    currentTime.value = currentTime.value?.minus(tick)
                }

                override fun onFinish() {
                    timerState.value=TimerState.Finished
                }

            }

            timerState.value = TimerState.Running
            timer.start()
        }

        fun stopTimer () {
            if (this::timer.isInitialized) timer.cancel()
            timerLenght = currentTime.value ?:0
            timerState.value = TimerState.Stopped
        }

        fun resetTimer () {
            currentTime.value = timerLenght
            //timerState = TimerState.Stopped
            if (this::timer.isInitialized) timer.cancel()
        }

        fun clearTimer () {
            timerLenght = 0
            timerState.value = TimerState.Stopped
            resetTimer()
        }

        fun secondsRemaining () : Long {
            if (timerLenght == UPPER_TIME_LIMIT) return UPPER_TIME_LIMIT - currentTime.value!!
            else return currentTime.value!!
        }

        fun resumeTimer(remainingTime: Long) {
            Log.i("labtimer","timerlength = $timerLenght, remainingTime = $remainingTime")
            if (timerLenght != UPPER_TIME_LIMIT) {
                val holder = timerLenght
                timerLenght = remainingTime
                currentTime.value = timerLenght
                startTimer()
                timerLenght = holder
            }
            else { //runup timer
                Log.i("labtimer","runup")
                timerLenght = 0L
                startTimer()
                currentTime.value= UPPER_TIME_LIMIT-remainingTime
            }

        }

    }
}