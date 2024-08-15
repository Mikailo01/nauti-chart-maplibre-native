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

    fun excludeDescriptionKeysFromTags(tagsMap: Map<String, String>): String {
        val keys = listOf("addr:", "ref:", "check_date", "name", "url", "amenity", "wikidata",
            "wikipedia", "created_by", "wheelchair", "tourism", "email", "website", "phone", "source",
            "internet_access", "fax", "image", "smoking", "contact", "mobile", "survey", "toilets", "leisure"
        )
        val stringList: MutableList<String> = mutableListOf()

        val matchedKeys = tagsMap.keys.filterNot { key -> keys.any { key.contains(it) } }

        for (key in matchedKeys) {
            tagsMap[key]?.let { value -> stringList.add("${formatTagString(key)}: $value") }
        }

        return stringList.joinToString("\n")
    }

    fun extractContactsFromTags(tagsMap: Map<String, String>): String {
        val subStrings: List<String> =
            listOf("website", "phone", "email", "facebook", "instagram", "fax", "url")
        val stringList: MutableList<String> = mutableListOf()
        var matchedSubString = ""

        for (tag in tagsMap) {
            val matchingKey = if (subStrings.any { subString ->
                    tag.key.contains(subString).takeIf { it }
                        ?.also { matchedSubString = subString } == true
                }) tag.key else null
            tagsMap[matchingKey]?.let { value -> stringList.add("${formatTagString(matchedSubString)}: $value") }
        }

        return stringList.joinToString("\n")
    }
}