package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bytecause.core.resources.R
import com.bytecause.domain.util.SearchTypes
import com.bytecause.search.ui.model.parcelable.PoiCategoryModel
import com.bytecause.util.string.UiText

class SearchMapCategoriesViewModel : ViewModel() {

    val categoryList = listOf(
        PoiCategoryModel(
            drawableId = R.drawable.rent,
            name = UiText.StringResource(R.string.rent),
            search = SearchTypes.Amenity(listOf("boat_rental", "bicycle_rental", "car_rental"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bench,
            name = UiText.StringResource(R.string.bench),
            search = SearchTypes.Amenity(listOf("bench"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.air_icon,
            name = UiText.StringResource(R.string.compressed_air),
            search = SearchTypes.Amenity(listOf("compressed_air"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.drinking_water,
            name = UiText.StringResource(R.string.drinking_water),
            search = SearchTypes.Amenity(listOf("drinking_water"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.toilets,
            name = UiText.StringResource(R.string.toilets),
            search = SearchTypes.Amenity(listOf("toilets"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.parking,
            name = UiText.StringResource(R.string.parking),
            search = SearchTypes.Amenity(listOf("parking"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.restaurant,
            name = UiText.StringResource(R.string.food),
            search = SearchTypes.Amenity(listOf("restaurant", "fast_food", "bbq"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cafe,
            name = UiText.StringResource(R.string.cafe),
            search = SearchTypes.Amenity(listOf("cafe"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.hospital,
            name = UiText.StringResource(R.string.health),
            search = SearchTypes.Amenity(
                listOf(
                    "hospital",
                    "clinic",
                    "pharmacy",
                    "doctors",
                    "dentist"
                )
            )
        ),
        PoiCategoryModel(
            drawableId = R.drawable.park,
            name = UiText.StringResource(R.string.park),
            search = SearchTypes.Amenity(listOf("park"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bar,
            name = UiText.StringResource(R.string.drink),
            search = SearchTypes.Amenity(listOf("bar", "pub"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.study,
            name = UiText.StringResource(R.string.study),
            search = SearchTypes.Amenity(listOf("school", "university", "college"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.theatre,
            name = UiText.StringResource(R.string.theatre),
            search = SearchTypes.Amenity(listOf("theatre"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cinema,
            name = UiText.StringResource(R.string.cinema),
            search = SearchTypes.Amenity(listOf("cinema"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.place_of_worship,
            name = UiText.StringResource(R.string.worship_and_memorial_grounds),
            search = SearchTypes.Amenity(listOf("place_of_worship"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.fuel_station,
            name = UiText.StringResource(R.string.fuel_station),
            search = SearchTypes.Amenity(listOf("fuel", "charging_station"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.charging_station,
            name = UiText.StringResource(R.string.charging_station),
            search = SearchTypes.Amenity(listOf("charging_station"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.money_icon,
            name = UiText.StringResource(R.string.finance),
            search = SearchTypes.Amenity(listOf("atm", "bank", "bureau_de_change"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.nightclub,
            name = UiText.StringResource(R.string.nightclub),
            search = SearchTypes.Amenity(listOf("nightclub"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.library,
            name = UiText.StringResource(R.string.library),
            search = SearchTypes.Amenity(listOf("library"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shower,
            name = UiText.StringResource(R.string.shower),
            search = SearchTypes.Amenity(listOf("shower"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.sea_waves,
            name = UiText.StringResource(R.string.nautical),
            search = SearchTypes.UnionSet(listOf("seamark:type"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shop,
            name = UiText.StringResource(R.string.shop),
            search = SearchTypes.UnionSet(listOf("shop"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.tourism,
            name = UiText.StringResource(R.string.tourism),
            search = SearchTypes.UnionSet(listOf("tourism"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bus_station,
            name = UiText.StringResource(R.string.public_transport),
            search = SearchTypes.UnionSet(listOf("public_transport"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.leisure,
            name = UiText.StringResource(R.string.leisure),
            search = SearchTypes.UnionSet(listOf("leisure"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.accommodation,
            name = UiText.StringResource(R.string.accommodation),
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