package com.bytecause.nautichart.util

import android.os.SystemClock

class Util {
    private var lastClick: Long = 0L
    fun lastClick(delay: Int): Boolean {
        return if (SystemClock.elapsedRealtime() - lastClick < delay) {
            false
        } else {
            lastClick = SystemClock.elapsedRealtime()
            true
        }
    }

}