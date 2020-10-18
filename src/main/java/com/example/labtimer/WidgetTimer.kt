package com.example.labtimer

import android.os.SystemClock

data class Timer(
    var running: Boolean,
    var timerLength: Long,
    var target: Long
)

class WidgetTimer {
    companion object {
        val timerMap = mutableMapOf<Int, Timer>()
        val defaultTimer = Timer(false, 0, 0)
        var lastClickTime = 0L

        fun clearWidgetTimer(Id: Int) {
            stopWidgetTimer(Id,0)
        }

        fun stopWidgetTimer(Id: Int, residualTime: Long = residualTime(Id)) {
            timerMap[Id]?.timerLength = residualTime
            timerMap[Id]?.running = false
        }

        fun addTime(Id: Int, time: Int){
            stopWidgetTimer(Id)
            timerMap[Id] = Timer(timerMap[Id]?.running ?:false, timerMap[Id]?.timerLength?.plus(time*1000) ?:0, timerMap[Id]?.target ?:0)
        }

        private fun residualTime (Id:Int ) :Long {
            if (timerMap[Id]?.running == true) {
                return timerMap[Id]?.target?.minus(SystemClock.elapsedRealtime()) ?: 0
            }
            else {
                return timerMap[Id]?.timerLength ?:0
            }
        }

    }
}
