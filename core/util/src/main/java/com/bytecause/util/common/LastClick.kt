package com.bytecause.util.common

import android.os.SystemClock

class LastClick {
    private var lastClick: Long = 0L
    fun lastClick(delay: Int = 1000): Boolean {
        return if (SystemClock.elapsedRealtime() - lastClick < delay) {
            false
        } else {
            lastClick = SystemClock.elapsedRealtime()
            true
        }
    }
}