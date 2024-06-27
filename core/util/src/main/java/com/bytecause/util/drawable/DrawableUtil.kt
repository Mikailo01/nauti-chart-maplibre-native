package com.bytecause.util.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.bytecause.core.resources.R
import com.bytecause.domain.util.PoiTagsUtil.formatTagString
import com.bytecause.util.poi.PoiUtil

object DrawableUtil {
    fun getResourceIds(namesList: List<String>): Map<String, Int> {
        val resourceIds = mutableMapOf<String, Int>()
        val fields = R.drawable::class.java.declaredFields
        for (fieldName in namesList) {
            for (field in fields) {
                if (formatTagString(field.name)
                        ?.lowercase() == PoiUtil.unifyPoiDrawables(fieldName).lowercase() ||
                    fieldName.split(" ").contains(formatTagString(field.name))
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

    fun getResourceName(categoryName: String?): String? {
        categoryName ?: return null

        val fields = R.drawable::class.java.declaredFields
        for (field in fields) {
            if (formatTagString(field.name)
                    ?.lowercase() == PoiUtil.unifyPoiDrawables(categoryName).lowercase() ||
                categoryName.split(" ").contains(formatTagString(field.name))
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

    fun drawCircle(context: Context, colorList: List<Int>): Bitmap {
        val density = context.resources.displayMetrics.density

        val radius = if (colorList.size < 1000) 12f * density else 12f * 1.4f * density
        val bitmap = Bitmap.createBitmap(
            (radius * 2).toInt(),
            (radius * 2).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            strokeWidth = 3f
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        // if colorList contains more than 1 color, divide 360 by the size of the list, which will
        // creates circle graph representing how many vessel's types are in corresponding area.
        val degree = if (colorList.size > 1) {
            360f / colorList.size
        } else {
            360f
        }

        var currentAngle = 0f

        // sort colorList to make circle graph clusterer consistent.
        for (color in colorList.sorted()) {
            paint.color = color
            canvas.drawArc(rect, currentAngle, degree, true, paint)
            currentAngle += degree
        }
        return bitmap
    }
}