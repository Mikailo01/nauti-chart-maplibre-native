package com.bytecause.nautichart.domain.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

sealed class OverpassElement

data class OverpassResponse(
    val version: Double,
    val generator: String,
    val osm3s: Osm3s,
    val elements: List<OverpassElement>
)

data class Osm3s(
    @SerializedName("timestamp_osm_base") val timestampOsmBase: String,
    val copyright: String
)

data class OverpassNodeModel(
    val type: String,
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>
): OverpassElement()

data class OverpassRelationModel(
    val type: String,
    val id: Long,
    val tags: Map<String, String>
): OverpassElement()

class OverpassElementTypeAdapter : TypeAdapter<OverpassElement>() {
    override fun write(writer: JsonWriter?, value: OverpassElement?) {
        // Serialization is not needed for this example
    }

    override fun read(reader: JsonReader?): OverpassElement? {
        if (reader != null) {
            val jsonObject = Gson().fromJson<JsonObject>(reader, JsonObject::class.java)

            return when {
                jsonObject.has("lat") && jsonObject.has("lon") -> Gson().fromJson(jsonObject, OverpassNodeModel::class.java)
                jsonObject.has("id") && jsonObject.has("tags") -> Gson().fromJson(jsonObject, OverpassRelationModel::class.java)
                else -> null
            }
        }
        return null
    }
}

class OverpassElementTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? {
        if (type?.rawType == OverpassElement::class.java) {
            return OverpassElementTypeAdapter() as TypeAdapter<T>
        }
        return null
    }
}


