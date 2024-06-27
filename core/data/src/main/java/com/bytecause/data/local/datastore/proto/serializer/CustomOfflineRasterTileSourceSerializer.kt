package com.bytecause.data.local.datastore.proto.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.CustomOfflineRasterTileSourceList
import java.io.InputStream
import java.io.OutputStream

object CustomOfflineRasterTileSourceSerializer : Serializer<CustomOfflineRasterTileSourceList> {
    override val defaultValue: CustomOfflineRasterTileSourceList
        get() = CustomOfflineRasterTileSourceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): CustomOfflineRasterTileSourceList {
        try {
            return CustomOfflineRasterTileSourceList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: CustomOfflineRasterTileSourceList, output: OutputStream) {
        t.writeTo(output)
    }
}