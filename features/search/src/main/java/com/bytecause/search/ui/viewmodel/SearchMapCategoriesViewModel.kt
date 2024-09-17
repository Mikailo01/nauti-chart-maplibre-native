package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bytecause.core.resources.R
import com.bytecause.domain.util.PoiTagsUtil.unformatTagString
import com.bytecause.domain.util.SearchTypes
import com.bytecause.search.ui.model.serializable.PoiCategoryModel
import com.bytecause.util.poi.PoiUtil.getCategoriesUnderUnifiedCategory
import com.bytecause.util.string.UiText

class SearchMapCategoriesViewModel : ViewModel() {

    val categoryList = listOf(
        PoiCategoryModel(
            drawableId = R.drawable.rent,
            name = UiText.StringResource(R.string.rent).resId,
            search = SearchTypes.Amenity(listOf("boat_rental", "bicycle_rental", "car_rental"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bench,
            name = UiText.StringResource(R.string.bench).resId,
            search = SearchTypes.Amenity(listOf("bench"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.air_icon,
            name = UiText.StringResource(R.string.compressed_air).resId,
            search = SearchTypes.Amenity(listOf("compressed_air"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.drinking_water,
            name = UiText.StringResource(R.string.water).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.water)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.toilets,
            name = UiText.StringResource(R.string.toilets).resId,
            search = SearchTypes.Amenity(listOf("toilets"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.parking,
            name = UiText.StringResource(R.string.parking).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.parking)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.restaurant,
            name = UiText.StringResource(R.string.food).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.food)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cafe,
            name = UiText.StringResource(R.string.cafe).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.cafe)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.hospital,
            name = UiText.StringResource(R.string.health).resId,
            search = SearchTypes.Amenity(
                getCategoriesUnderUnifiedCategory(R.string.health)?.map {
                    unformatTagString(it)
                } ?: emptyList()
            )
        ),
        PoiCategoryModel(
            drawableId = R.drawable.nature,
            name = UiText.StringResource(R.string.nature).resId,
            search = SearchTypes.UnionSet(
                listOf("leisure")
            ).filter(getCategoriesUnderUnifiedCategory(R.string.nature)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bar,
            name = UiText.StringResource(R.string.drink).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.drink)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.study,
            name = UiText.StringResource(R.string.study).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.study)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.theatre,
            name = UiText.StringResource(R.string.theatre).resId,
            search = SearchTypes.Amenity(listOf("theatre"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cinema,
            name = UiText.StringResource(R.string.cinema).resId,
            search = SearchTypes.Amenity(listOf("cinema"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.place_of_worship,
            name = UiText.StringResource(R.string.worship_and_memorial_grounds).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.worship_and_memorial_grounds)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.fuel_station,
            name = UiText.StringResource(R.string.fuel_station).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.fuel_station)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.charging_station,
            name = UiText.StringResource(R.string.charging_station).resId,
            search = SearchTypes.Amenity(listOf("charging_station"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.money_icon,
            name = UiText.StringResource(R.string.finance).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.finance)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.nightclub,
            name = UiText.StringResource(R.string.nightclub).resId,
            search = SearchTypes.Amenity(listOf("nightclub"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.library,
            name = UiText.StringResource(R.string.library).resId,
            search = SearchTypes.Amenity(getCategoriesUnderUnifiedCategory(R.string.library)?.map {
                unformatTagString(it)
            } ?: emptyList())
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shower,
            name = UiText.StringResource(R.string.shower).resId,
            search = SearchTypes.Amenity(listOf("shower"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.sea_waves,
            name = UiText.StringResource(R.string.nautical).resId,
            search = SearchTypes.UnionSet(listOf("seamark:type"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shop,
            name = UiText.StringResource(R.string.shop).resId,
            search = SearchTypes.UnionSet(listOf("shop"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.tourism,
            name = UiText.StringResource(R.string.tourism).resId,
            search = SearchTypes.UnionSet(listOf("tourism"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bus_station,
            name = UiText.StringResource(R.string.public_transport).resId,
            search = SearchTypes.UnionSet(listOf("public_transport"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.leisure,
            name = UiText.StringResource(R.string.leisure).resId,
            search = SearchTypes.UnionSet(listOf("leisure"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.accommodation,
            name = UiText.StringResource(R.string.accommodation).resId,
            search = SearchTypes.UnionSet(listOf("tourism")).filter(
                listOf(
                    "apartment",
                    "hostel",
                    "hotel",
                    "love_hotel",
                    "chalet",
                    "guest_house",
                    "motel",
                    "wilderness_hut",
                    "alpine_hut"
                )
            )
        )
    )
}