package com.bytecause.data.local.datastore.proto.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.bytecause.nautichart.CustomOnlineRasterTileSourceList
import java.io.InputStream
import java.io.OutputStream

object CustomOnlineRasterTileSourceSerializer : Serializer<CustomOnlineRasterTileSourceList> {
    override val defaultValue: CustomOnlineRasterTileSourceList
        get() = CustomOnlineRasterTileSourceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): CustomOnlineRasterTileSourceList {
        try {
            return CustomOnlineRasterTileSourceList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: CustomOnlineRasterTileSourceList, output: OutputStream) {
        t.writeTo(output)
    }
}