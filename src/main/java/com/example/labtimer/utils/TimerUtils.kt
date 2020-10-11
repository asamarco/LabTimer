package com.example.labtimer.utils

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
        var timerLength: Long = 0 //milliseconds
        var progress = MutableLiveData<Int>()

        init {
            currentTime.value = 0
            timerState.value = TimerState.Stopped
        }

        fun startTimer () {
            var tick = 1L //+1 for countdown timer, -1 for runup timer
            timerLength = currentTime.value ?:0

            if (timerLength == 0L) { // starts runup timer
                timerLength = UPPER_TIME_LIMIT; tick = -1L}
            Log.i("labtimer", "timerLength = $timerLength")

            timer = object : CountDownTimer(timerLength *1000, ONE_SECOND) {

                override fun onTick(millisUntilFinished: Long) {
                    currentTime.value = currentTime.value?.minus(tick)
                    progress.value = progress(timerLength, currentTime.value!!)
                }

                override fun onFinish() {
                    timerState.value= TimerState.Finished
                }

            }

            timerState.value = TimerState.Running
            timer.start()
        }

        private fun progress (length: Long, remainingTime: Long): Int {
            return ((length-remainingTime)*100/length).toInt()
        }

        fun stopTimer () {
            if (this::timer.isInitialized) timer.cancel()
            timerState.value = TimerState.Stopped
        }

        fun resetTimer () {//recall the stored timer length without stopping
            currentTime.value = timerLength
            if (this::timer.isInitialized) timer.cancel()
        }

        fun clearTimer () {
            timerLength = 0
            timerState.value = TimerState.Stopped
            resetTimer()
        }

        fun secondsRemaining () : Long {
            if (timerLength == UPPER_TIME_LIMIT) return UPPER_TIME_LIMIT - currentTime.value!!
            else return currentTime.value!!
        }

        fun resumeTimer(remainingTime: Long, timerLengthStored: Long) {
            timerLength = timerLengthStored
            Log.i("labtimer","timerlength = $timerLength, remainingTime = $remainingTime")
            if (timerLength != UPPER_TIME_LIMIT) {//countdown timer
                val holder = timerLength
                timerLength = remainingTime
                currentTime.value = timerLength
                startTimer()
                timerLength = holder //stores the original timerlength so that it can be recalled later with resetTimer
            }
            else { //runup timer
                Log.i("labtimer","runup")
                timerLength = 0L
                startTimer()
                currentTime.value= UPPER_TIME_LIMIT -remainingTime
            }

        }

    }
}