package com.bytecause.nautichart.data.local.datastore.proto.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.CustomOnlineTileSourceList
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import java.io.InputStream
import java.io.OutputStream

object CustomOnlineTileSourceSerializer : Serializer<CustomOnlineTileSourceList> {
    override val defaultValue: CustomOnlineTileSourceList
        get() = CustomOnlineTileSourceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): CustomOnlineTileSourceList {
        try {
            return CustomOnlineTileSourceList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: CustomOnlineTileSourceList, output: OutputStream) {
        t.writeTo(output)
    }
}