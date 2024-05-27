package com.bytecause.nautichart.data.local.datastore.proto.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.CustomOfflineTileSourceList
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import java.io.InputStream
import java.io.OutputStream

object CustomOfflineTileSourceSerializer : Serializer<CustomOfflineTileSourceList> {
    override val defaultValue: CustomOfflineTileSourceList
        get() = CustomOfflineTileSourceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): CustomOfflineTileSourceList {
        try {
            return CustomOfflineTileSourceList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: CustomOfflineTileSourceList, output: OutputStream) {
        t.writeTo(output)
    }
}