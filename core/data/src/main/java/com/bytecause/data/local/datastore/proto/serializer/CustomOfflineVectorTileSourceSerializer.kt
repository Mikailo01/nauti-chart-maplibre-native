package com.bytecause.data.local.datastore.proto.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.CustomOfflineVectorTileSourceList
import java.io.InputStream
import java.io.OutputStream

object CustomOfflineVectorTileSourceSerializer : Serializer<CustomOfflineVectorTileSourceList> {

    override val defaultValue: CustomOfflineVectorTileSourceList
        get() = CustomOfflineVectorTileSourceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): CustomOfflineVectorTileSourceList {
        try {
            return CustomOfflineVectorTileSourceList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: CustomOfflineVectorTileSourceList, output: OutputStream) {
        t.writeTo(output)
    }
}