package com.bytecause.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bytecause.core.resources.R
import com.bytecause.domain.util.SearchTypes
import com.bytecause.search.ui.model.parcelable.PoiCategoryModel

class SearchMapCategoriesViewModel : ViewModel() {

    val categoryList = listOf(
        PoiCategoryModel(
            drawableId = R.drawable.rent,
            name = "Rent",
            search = SearchTypes.Amenity(listOf("boat_rental", "bicycle_rental", "car_rental"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bench,
            name = "Bench",
            search = SearchTypes.Amenity(listOf("bench"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.air_icon,
            name = "Compressed air",
            search = SearchTypes.Amenity(listOf("compressed_air"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.drinking_water,
            name = "Drinking water",
            search = SearchTypes.Amenity(listOf("drinking_water"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.toilets,
            name = "Toilets",
            search = SearchTypes.Amenity(listOf("toilets"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.parking,
            name = "Parking",
            search = SearchTypes.Amenity(listOf("parking"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.restaurant,
            name = "Food",
            search = SearchTypes.Amenity(listOf("restaurant", "fast_food"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cafe,
            name = "Cafe",
            search = SearchTypes.Amenity(listOf("cafe"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.hospital,
            name = "Health",
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
            name = "Park",
            search = SearchTypes.Amenity(listOf("park"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bar,
            name = "Drink",
            search = SearchTypes.Amenity(listOf("bar", "pub"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.study,
            name = "Study",
            search = SearchTypes.Amenity(listOf("school", "university", "college"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.theatre,
            name = "Theatre",
            search = SearchTypes.Amenity(listOf("theatre"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cinema,
            name = "Cinema",
            search = SearchTypes.Amenity(listOf("cinema"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.place_of_worship,
            name = "Place of worship",
            search = SearchTypes.Amenity(listOf("place_of_worship"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.fuel,
            name = "Fuel station",
            search = SearchTypes.Amenity(listOf("fuel"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.charging_station,
            name = "Charging station",
            search = SearchTypes.Amenity(listOf("charging_station"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.money_icon,
            name = "Finance",
            search = SearchTypes.Amenity(listOf("atm", "bank", "bureau_de_change"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.nightclub,
            name = "Nightclub",
            search = SearchTypes.Amenity(listOf("nightclub"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.library,
            name = "Library",
            search = SearchTypes.Amenity(listOf("library"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shower,
            name = "Public shower",
            search = SearchTypes.Amenity(listOf("shower"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.sea_waves,
            name = "Sea marks",
            search = SearchTypes.UnionSet(listOf("seamark:type"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shop,
            name = "Shops",
            search = SearchTypes.UnionSet(listOf("shop"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.tourism,
            name = "Tourism",
            search = SearchTypes.UnionSet(listOf("tourism"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bus_station,
            name = "Public transport",
            search = SearchTypes.UnionSet(listOf("public_transport"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.activity,
            name = "Activity",
            search = SearchTypes.UnionSet(listOf("leisure"))
        )
    ).sortedBy { it.name }
}