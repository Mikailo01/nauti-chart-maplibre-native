package com.bytecause.nautichart.ui.util

import com.bytecause.nautichart.R
import com.bytecause.nautichart.util.PoiUtil
import com.bytecause.nautichart.util.StringUtil

class DrawableUtil {

    companion object {
        fun getResourceIds(namesList: List<String>): Map<String, Int> {
            val resourceIds = mutableMapOf<String, Int>()
            val fields = R.drawable::class.java.declaredFields
            for (fieldName in namesList) {
                for (field in fields) {
                    if (StringUtil.formatTagString(field.name)
                            ?.lowercase() == PoiUtil().unifyPoiDrawables(fieldName).lowercase() ||
                        fieldName.split(" ").contains(StringUtil.formatTagString(field.name))
                    ) {
                        try {
                            val resId = field.getInt(null)
                            resourceIds[fieldName] = resId
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        break // Break once the resource ID is found for this particular name
                    }
                }
            }
            return resourceIds
        }

        fun assignDrawableToAddressType(addressType: String): Int {
            return when (addressType) {
                "city" -> R.drawable.city_icon
                "village" -> R.drawable.village_icon
                "house" -> R.drawable.house_icon
                "town" -> R.drawable.town_icon
                else -> R.drawable.map_marker
            }
        }

        fun getResourceName(categoryName: String): String {
            val fields = R.drawable::class.java.declaredFields
            for (field in fields) {
                if (StringUtil.formatTagString(field.name)
                        ?.lowercase() == PoiUtil().unifyPoiDrawables(categoryName).lowercase() ||
                    categoryName.split(" ").contains(StringUtil.formatTagString(field.name))
                ) {
                    return field.name
                }
            }
            return ""
        }

        fun assignDrawableColorToPoiCategory(category: String): Int {
            return when (category) {
                "Cafe", "Restaurant", "Fast food" -> R.color.poi_yellow
                "Public transport" -> R.color.poi_dark_blue
                "Shop", "Car rent", "Boat rent", "Bicycle rent" -> R.color.poi_medium_blue
                "Fuel", "Charging station" -> R.color.poi_light_blue
                "Nightclub", "Cinema", "Theatre", "Tourism", "Park", "Swimming pool" -> R.color.turquoise
                "Bar", "Pub", "Ferry terminal", "Toilets", "Public shower", "Atm", "Bank", "Library", "Fire station", "Veterinary", "Dentist", "Doctors", "Hospital" -> R.color.poi_dark_red
                "Health" -> R.color.poi_light_red
                "Accommodation" -> R.color.poi_pink
                "Activity" -> R.color.poi_light_green
                else -> R.color.poi_gray
            }
        }
    }
}