package com.bytecause.search.data.local.datastore.proto.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.RecentlySearchedPlaceList
import java.io.InputStream
import java.io.OutputStream


object RecentlySearchedPlaceSerializer: Serializer<RecentlySearchedPlaceList> {

    override val defaultValue: RecentlySearchedPlaceList
        get() = RecentlySearchedPlaceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): RecentlySearchedPlaceList {
        try {
            return RecentlySearchedPlaceList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: RecentlySearchedPlaceList, output: OutputStream) {
        t.writeTo(output)
    }
}