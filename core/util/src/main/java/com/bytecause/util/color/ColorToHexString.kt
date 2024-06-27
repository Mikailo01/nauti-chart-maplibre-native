package com.bytecause.util.color

import androidx.annotation.ColorInt
import java.lang.String.format


fun @receiver:ColorInt Int.toHexString(): String = format("#%06X", (0xFFFFFF and this))
