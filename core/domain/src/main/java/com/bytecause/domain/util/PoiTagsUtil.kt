package com.bytecause.domain.util

object PoiTagsUtil {
    fun formatTagString(s: String?): String? {
        s ?: return null
        if (s.contains(";")) {
            return s.replace("_", " ").replace(";", ", ")
                .replaceFirstChar { it.uppercase() }
        }
        return s.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    /** extracts category from overpass element's tags **/
    fun extractCategoryFromPoiEntity(tagsMap: Map<String, String>): String? {
        return when {
            tagsMap.containsKey("amenity") -> tagsMap["amenity"]
            tagsMap.containsKey(
                "bus",
            ) && tagsMap.containsKey("railway") -> if (tagsMap["railway"] != "stop") "bus_stop & ${tagsMap["railway"]}" else "bus"

            tagsMap.containsKey("bus") && tagsMap.containsKey("trolleybus") -> "trolleybus"
            tagsMap.containsKey("highway") && tagsMap.containsKey("railway") -> "${tagsMap["railway"]} & ${tagsMap["highway"]}"
            tagsMap.containsKey("highway") && tagsMap.containsKey("trolleybus") -> "${tagsMap["highway"]} & trolleybus_stop"
            tagsMap.containsKey("tram") -> "tram"
            tagsMap.containsKey("train") -> "train_${tagsMap["railway"] ?: "stop"}"
            tagsMap.containsKey("railway") -> tagsMap["railway"]
            tagsMap.containsKey("bus") -> "bus"
            tagsMap.containsKey("trolleybus") -> "trolleybus"
            tagsMap.containsKey("highway") -> tagsMap["highway"]
            tagsMap.containsKey("public_transport") -> tagsMap["public_transport"]
            tagsMap.containsKey("shop") -> "shop"
            tagsMap.containsKey("leisure") -> tagsMap["leisure"].takeIf { it != "yes" } ?: "leisure"
            tagsMap.containsKey("tourism") -> tagsMap["tourism"].takeIf { it != "yes" } ?: "tourism"
            tagsMap.containsKey("seamark:type") -> tagsMap["seamark:type"]
            else -> null
        }
    }
}