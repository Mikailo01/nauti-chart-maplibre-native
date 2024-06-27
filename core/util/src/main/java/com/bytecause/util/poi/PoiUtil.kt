package com.bytecause.util.poi

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import com.bytecause.core.resources.R
import com.bytecause.util.drawable.DrawableUtil

object PoiUtil {
    fun createLayerDrawable(
        context: Context,
        category: String,
        drawable: Drawable?,
    ): LayerDrawable {
        return (
            ContextCompat.getDrawable(
                context,
                R.drawable.universal_poi_marker_icon,
            ) as LayerDrawable
        ).apply {
            setTint(
                ContextCompat.getColor(
                    context,
                    DrawableUtil.assignDrawableColorToPoiCategory(
                        category,
                    ),
                ),
            )
            setDrawableByLayerId(
                R.id.top_layer,
                drawable,
            )
        }
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

    fun unifyPoiCategory(categoryName: String): List<String> {
        val unifyCategoryMap =
            mapOf(
                "Study" to listOf("School", "College", "University", "Kindergarten"),
                "Accommodation" to
                    listOf(
                        "Guest house",
                        "Hotel",
                        "Love hotel",
                        "Hostel",
                        "Apartment",
                        "Motel",
                    ),
                "Health" to
                    listOf(
                        "Hospital",
                        "Dentist",
                        "Doctors",
                        "Pharmacy",
                        "First aid",
                        "Clinic",
                    ),
                "Finance" to listOf("Bank", "Bureau de change", "Atm"),
                "Public transport" to
                    listOf(
                        "Bus station",
                        "Ferry terminal",
                        "Bus",
                        "Bus stop & tram stop",
                        "Bus stop",
                        "Train stop",
                        "Train station",
                        "Tram",
                        "Tram stop & bus stop",
                        "Trolleybus",
                        "Trolley bay",
                        "Train halt",
                        "Station",
                        "Bus stop & trolleybus stop",
                    ),
                "Drink" to listOf("Pub", "Bar"),
                "Food" to listOf("Restaurant", "Fast food"),
                "Rent" to listOf("Car rental", "Boat rental", "Bicycle rental"),
                "Tourism" to
                    listOf(
                        "Information",
                        "Gallery",
                        "Viewpoint",
                        "Artwork",
                        "Attraction",
                        "Picnic site",
                        "Zoo",
                        "Camp site",
                        "Shelter",
                        "Museum",
                        "Fountain",
                        "Caravan site",
                        "Clock",
                    ),
                "Fuel station" to listOf("Fuel"),
                "Shops" to listOf("Shop"),
                "Public shower" to listOf("Shower"),
                "Activity" to listOf("Playground"),
            )

        for (map in unifyCategoryMap) {
            if (map.key == categoryName) {
                return map.value
            } else if (map.value.contains(categoryName)) {
                return listOf(map.key)
            }
        }

        // return back passed argument
        return listOf(categoryName)
    }

    // for category names listed in list will be used the same drawable resource.
    fun unifyPoiDrawables(categoryName: String): String {
        val unifyCategoryMap =
            mapOf(
                "Study" to listOf("School", "College", "University"),
                "Accommodation" to listOf("Guest house", "Hotel", "Hostel"),
                "Health" to listOf("Doctors", "Dentist", "Clinic"),
            )

        for (map in unifyCategoryMap) {
            if (map.value.contains(categoryName)) return map.key
        }

        return categoryName
    }

    fun extractRegionFromQuery(query: String): String? {
        val regionRegex = """area\["name"="(.*?)"]->\.searchArea""".toRegex()
        val matchResult = regionRegex.find(query)
        return matchResult?.groupValues?.get(1)
    }

    // unify methods of payment listed in overpass element's tags
    fun generalizeTagKeys(elementList: List<com.bytecause.domain.model.OverpassNodeModel>?): List<com.bytecause.domain.model.OverpassNodeModel> {
        val stringMap =
            mapOf(
                "payment" to
                    mapOf(
                        "Credit cards" to
                            listOf(
                                "card",
                                "cards",
                                "contactless",
                                "credit_cards",
                                "debit_cards",
                                "maestro",
                                "mastercard",
                                "visa",
                                "visa_debit",
                                "visa_electron",
                                "american_express",
                                "diners_club",
                                "discover_card",
                                "jcb",
                            ),
                        "Crypto payment" to
                            listOf(
                                "lightning",
                                "lightning_contactless",
                                "onchain",
                            ),
                        "QR payment" to listOf("qerko"),
                    ),
            )

        return elementList?.map { element ->
            val modifiedTags = mutableMapOf<String, String>()
            element.tags.forEach { (key, value) ->
                val parts = key.split(':')
                if (parts.size >= 2 && parts[0] in stringMap.keys) {
                    stringMap[parts[0]]?.forEach innerForEach@{
                        if (it.value.contains(parts[1])) {
                            modifiedTags[it.key] = value
                            return@innerForEach
                        }
                    }
                } else {
                    modifiedTags[key] = value
                }
            }
            element.copy(tags = modifiedTags)
        } ?: listOf()
    }
}
