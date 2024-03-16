package com.bytecause.nautichart.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

object NetworkUtil {

    @Suppress("SpellCheckingInspection")
    suspend fun isOnline(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val timeoutMs = 1500
                val sock = Socket()
                val sockAddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)

                sock.connect(sockAddr, timeoutMs)
                sock.close()

                true
            } catch (e: IOException) {
                false
            }
        }
    }
}