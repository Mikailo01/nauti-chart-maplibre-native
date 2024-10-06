package com.bytecause.map.data.local.datastore.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.AnchorageHistoryList
import java.io.InputStream
import java.io.OutputStream


object AnchorageHistorySerializer : Serializer<AnchorageHistoryList> {

    override val defaultValue: AnchorageHistoryList
        get() = AnchorageHistoryList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AnchorageHistoryList {
        try {
            return AnchorageHistoryList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: AnchorageHistoryList, output: OutputStream) {
        t.writeTo(output)
    }
}