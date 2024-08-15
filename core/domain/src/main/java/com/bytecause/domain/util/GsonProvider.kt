package com.bytecause.domain.util

import com.google.gson.Gson

// Gson instance should be cached, that's why I introduced this helper singleton object
object GsonProvider {
    val gson: Gson = Gson()
}