package com.bytecause.search.util

import com.bytecause.search.ui.model.PoiUiModel

object PoiTagsUtil {
    // unify methods of payment listed in overpass element's tags
    fun generalizeTagKeys(elementList: List<PoiUiModel>?): List<PoiUiModel> {
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