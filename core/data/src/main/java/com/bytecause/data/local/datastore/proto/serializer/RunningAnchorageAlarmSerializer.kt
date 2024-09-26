package com.bytecause.data.local.datastore.proto.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.RunningAnchorageAlarm
import java.io.InputStream
import java.io.OutputStream

object RunningAnchorageAlarmSerializer: Serializer<RunningAnchorageAlarm> {

    override val defaultValue: RunningAnchorageAlarm
    get() = RunningAnchorageAlarm.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): RunningAnchorageAlarm {
        try {
            return RunningAnchorageAlarm.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: RunningAnchorageAlarm, output: OutputStream) {
        t.writeTo(output)
    }
}