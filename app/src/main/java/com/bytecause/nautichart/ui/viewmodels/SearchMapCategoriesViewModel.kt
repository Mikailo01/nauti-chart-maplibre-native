package com.bytecause.nautichart.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.bytecause.nautichart.R
import com.bytecause.nautichart.domain.model.parcelable.PoiCategoryModel
import com.bytecause.nautichart.util.SearchTypes

class SearchMapCategoriesViewModel : ViewModel() {

    val categoryList = listOf(
        PoiCategoryModel(
            drawableId = R.drawable.rent,
            name = "Rent",
            search = SearchTypes.Amenity(arrayOf("boat_rental", "bicycle_rental", "car_rental"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bench,
            name = "Bench",
            search = SearchTypes.Amenity(arrayOf("bench"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.air_icon,
            name = "Compressed air",
            search = SearchTypes.Amenity(arrayOf("compressed_air"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.drinking_water,
            name = "Drinking water",
            search = SearchTypes.Amenity(arrayOf("drinking_water"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.toilets,
            name = "Toilets",
            search = SearchTypes.Amenity(arrayOf("toilets"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.parking,
            name = "Parking",
            search = SearchTypes.Amenity(arrayOf("parking"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.restaurant,
            name = "Food",
            search = SearchTypes.Amenity(arrayOf("restaurant", "fast_food"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cafe,
            name = "Cafe",
            search = SearchTypes.Amenity(arrayOf("cafe"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.hospital,
            name = "Health",
            search = SearchTypes.Amenity(
                arrayOf(
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
            search = SearchTypes.Amenity(arrayOf("park"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bar,
            name = "Drink",
            search = SearchTypes.Amenity(arrayOf("bar", "pub"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.study,
            name = "Study",
            search = SearchTypes.Amenity(arrayOf("school", "university", "college"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.theatre,
            name = "Theatre",
            search = SearchTypes.Amenity(arrayOf("theatre"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.cinema,
            name = "Cinema",
            search = SearchTypes.Amenity(arrayOf("cinema"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.place_of_worship,
            name = "Place of worship",
            search = SearchTypes.Amenity(arrayOf("place_of_worship"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.fuel,
            name = "Fuel station",
            search = SearchTypes.Amenity(arrayOf("fuel"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.charging_station,
            name = "Charging station",
            search = SearchTypes.Amenity(arrayOf("charging_station"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.money_icon,
            name = "Finance",
            search = SearchTypes.Amenity(arrayOf("atm", "bank", "bureau_de_change"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.nightclub,
            name = "Nightclub",
            search = SearchTypes.Amenity(arrayOf("nightclub"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.library,
            name = "Library",
            search = SearchTypes.Amenity(arrayOf("library"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shower,
            name = "Public shower",
            search = SearchTypes.Amenity(arrayOf("shower"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.baseline_description_24,
            name = "Sea marks",
            search = SearchTypes.UnionSet(arrayOf("seamark:type"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.shop,
            name = "Shops",
            search = SearchTypes.UnionSet(arrayOf("shop"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.tourism,
            name = "Tourism",
            search = SearchTypes.UnionSet(arrayOf("tourism"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.bus_station,
            name = "Public transport",
            search = SearchTypes.UnionSet(arrayOf("public_transport"))
        ),
        PoiCategoryModel(
            drawableId = R.drawable.activity,
            name = "Activity",
            search = SearchTypes.UnionSet(arrayOf("leisure"))
        )
    ).sortedBy { it.name }


}