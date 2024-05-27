package com.bytecause.nautichart.data.local.datastore.proto.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList
import java.io.InputStream
import java.io.OutputStream

object RecentUsedPoiIconSerializer : Serializer<RecentlyUsedPoiMarkerIconList> {
    override val defaultValue: RecentlyUsedPoiMarkerIconList
        get() = RecentlyUsedPoiMarkerIconList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): RecentlyUsedPoiMarkerIconList {
        try {
            return RecentlyUsedPoiMarkerIconList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: RecentlyUsedPoiMarkerIconList, output: OutputStream) {
        t.writeTo(output)
    }
}