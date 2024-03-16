package com.bytecause.nautichart.data.remote

import android.util.Log
import com.bytecause.nautichart.util.CountryNameMapping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.util.Locale

class RegionDataExtractRemoteDataSource {

    // Gets html structure of Geofabrik Download Server and fetch size of each region
    suspend fun fetchRegionSize(region: String, country: String? = null): Map<String, String> =
        withContext(Dispatchers.IO) {
            val map = mutableMapOf<String, String>()
            val baseUrl = "https://download.geofabrik.de/"
            val urlBuilder = StringBuilder(baseUrl)
            urlBuilder.apply {
                append(region)
                if (country != null) append("/$country")
                append(".html")
            }

            try {
                val document = Jsoup.connect(urlBuilder.toString()).get()
                val table = document.select("tr[onmouseover]")
                table.forEach {
                    map.putAll(extractSizes(it.allElements.outerHtml(), !country.isNullOrEmpty()))
                }
                map
            } catch (e: IOException) {
                Log.e("mapfragment", "error")
                map
            }
        }

    private fun getIsoCodeFromCountryName(countryName: String): String {

        return CountryNameMapping.getCountryIso(countryName)

        /*var country = countryName
        val locales = Locale.getAvailableLocales()
        if (countryName.contains("-") && !countryName.contains(
                "united",
                ignoreCase = true
            )
        ) {
            country = countryName.split("-")[0]
        } else if (countryName.contains("-")) country = countryName.replace("-", " ")

        for (locale in locales) {
            if (locale.displayCountry.contains(country, ignoreCase = true)) {
                return locale.country
            }
        }

        return null*/
    }

    private fun extractSizes(input: String, isCountry: Boolean): Map<String, String> {
        val map = mutableMapOf<String, String>()

        val regexNamePattern = """<a\s+href="[^"]+">([^<]+)</a>"""
        val regexSizePattern = "\\((\\d+(?:\\.\\d+)?)&nbsp;([GMK]B)\\)"

        val nameRegex = Regex(regexNamePattern)
        val sizeRegex = Regex(regexSizePattern)

        val nameMatchResult = nameRegex.find(input)
        val sizeMatchResult = sizeRegex.find(input)

        if (sizeMatchResult != null && nameMatchResult != null) {
            val extractedNameValue =
                nameMatchResult.groupValues[1].split(".")[0]//.replace("-", " ")

            val extractedSizeValue = sizeMatchResult.groupValues[1]
            val extractedUnit = sizeMatchResult.groupValues[2]

            if (isCountry) {
                map[extractedNameValue.split(" ")[0]] = "$extractedSizeValue $extractedUnit"
            } else {
                map[getIsoCodeFromCountryName(extractedNameValue)] =
                    "$extractedSizeValue $extractedUnit"
            }
        }
        return map
    }
}