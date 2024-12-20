package com.bytecause.util.poi

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.bytecause.core.resources.R

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
                        assignDrawableColorToPoiCategory(
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

    private fun assignDrawableColorToPoiCategory(category: String): Int {
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

    val poiSymbolDrawableMap: Map<String, Int> = mapOf(
        "Bar" to R.drawable.bar,
        "Pub" to R.drawable.pub,
        "Accommodation" to R.drawable.accommodation,
        "Toilets" to R.drawable.toilets,
        "Shop" to R.drawable.shop,
        "Vending machine" to R.drawable.vending_machine,
        "Farm" to R.drawable.farm,
        "Information" to R.drawable.information,
        "Ice cream" to R.drawable.ice_cream,
        "Theatre" to R.drawable.theatre,
        "Bench" to R.drawable.bench,
        "Restaurant" to R.drawable.restaurant,
        "Fast food" to R.drawable.fast_food,
        "Atm" to R.drawable.atm,
        "Bank" to R.drawable.bank,
        "Artwork" to R.drawable.artwork,
        "Bowling alley" to R.drawable.bowling_alley,
        "Bridge" to R.drawable.bridge,
        "Boat rental" to R.drawable.boat_rent,
        "Bus" to R.drawable.bus_station,
        "Tram" to R.drawable.tram,
        "Trolleybus" to R.drawable.trolleybus,
        "Train" to R.drawable.train,
        "Animal" to R.drawable.animal,
        "Adult" to R.drawable.adult,
        "Bureau de change" to R.drawable.bureau_de_change,
        "Love hotel" to R.drawable.love_hotel,
        "Dentist" to R.drawable.dentist,
        "Pharmacy" to R.drawable.pharmacy,
        "First aid" to R.drawable.first_aid,
        "Hospital" to R.drawable.hospital,
        "Clinic" to R.drawable.clinic,
        "Doctors" to R.drawable.doctors,
        "Veterinary" to R.drawable.veterinary,
        "Golf" to R.drawable.golf,
        "Drinking water" to R.drawable.drinking_water,
        "Arts centre" to R.drawable.arts_centre,
        "Swimming" to R.drawable.swimming_pool,
        "Hunting stand" to R.drawable.hunting_stand,
        "Ferry terminal" to R.drawable.ferry_terminal,
        "Fire station" to R.drawable.fire_station,
        "Nightclub" to R.drawable.nightclub,
        "Cinema" to R.drawable.cinema,
        "Charging station" to R.drawable.charging_station,
        "Casino" to R.drawable.casino,
        "Grave yard" to R.drawable.grave_yard,
        "Horse riding" to R.drawable.horse_riding,
        "Firepit" to R.drawable.firepit,
        "Bicycle parking" to R.drawable.bicycle_parking,
        "Viewpoint" to R.drawable.viewpoint,
        "Cafe" to R.drawable.cafe,
        "Camp site" to R.drawable.camping,
        "Car rental" to R.drawable.car_rental,
        "Car wash" to R.drawable.car_wash,
        "Caravan site" to R.drawable.caravan_site,
        "Chalet" to R.drawable.chalet,
        "Care" to R.drawable.care,
        "Compressed air" to R.drawable.compressed_air,
        "Dog" to R.drawable.dog,
        "Fishing" to R.drawable.fishing,
        "Sport" to R.drawable.fitness,
        "Fuel" to R.drawable.fuel_station,
        "Zoo" to R.drawable.zoo,
        "Wreck" to R.drawable.wreck,
        "Vacuum cleaner" to R.drawable.vacuum_cleaner,
        "Taxi" to R.drawable.taxi,
        "Speed enforcement" to R.drawable.speed_enforcement,
        "Shower" to R.drawable.shower,
        "Shelter" to R.drawable.shelter,
        "Sauna" to R.drawable.sauna,
        "Police" to R.drawable.police,
        "Playground" to R.drawable.playground,
        "Place of worship" to R.drawable.place_of_worship,
        "Picnic" to R.drawable.picnic_table,
        "Parking" to R.drawable.parking,
        "Museum" to R.drawable.museum,
        "Library" to R.drawable.library,
        "Public bookcase" to R.drawable.public_bookcase,
        "Post office" to R.drawable.post_office,
        "Post" to R.drawable.post_box,
        "Nature" to R.drawable.nature,
        "Park" to R.drawable.park,
        "Ticket validator" to R.drawable.ticket_validator,
        "Tourism" to R.drawable.tourist,
        "Townhall" to R.drawable.townhall,
        "Slipway" to R.drawable.slipway,
        "Recycling" to R.drawable.recycling,
        "Binoculars" to R.drawable.binoculars,
        "Leisure" to R.drawable.leisure,
        "Waste" to R.drawable.waste_basket,
        "Mooring" to R.drawable.mooring,
        "Marina" to R.drawable.anchor
    )

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

    /**
     * Used for searching by radius.
     * **/
    fun unifyPoiCategoryForSearch(categoryName: String): List<String> {
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
                            "Veterinary"
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
                "Accommodation" to listOf(
                    "Hostel",
                    "Apartment",
                    "Motel",
                    "Guest house",
                    "Hotel"
                ),
                "Golf" to listOf(
                    "Miniature golf",
                    "Golf course"
                ),
                "Animal" to listOf(
                    "Animal boarding",
                    "Animal breeding",
                    "Animal shelter",
                    "Animal training",
                    "Wildlife feeding",
                    "Fodder rack",
                    "Feeding rack",
                    "Bird hide"
                ),
                "Bus" to listOf(
                    "Bus",
                    "Bus station",
                    "Bus stop"
                ),
                "Care" to listOf(
                    "Childcare",
                    "Nursing home"
                ),
                "Dog" to listOf(
                    "Dog park",
                    "Dog toilet"
                ),
                "Sport" to listOf(
                    "Fitness centre",
                    "Fitness station",
                    "Sports hall",
                    "Sport",
                    "Sport centre"
                ),
                "Tram" to listOf(
                    "Tram",
                    "Tram stop"
                ),
                "Train" to listOf(
                    "Train",
                    "Train stop",
                    "Train station",
                    "Train halt",
                ),
                "Picnic" to listOf(
                    "Picnic table",
                    "Picnic site"
                ),
                "Parking" to listOf(
                    "Parking",
                    "Parking space",
                    "Motorcycle parking"
                ),
                "Post" to listOf(
                    "Post box",
                    "Post depot",
                    "Letter box"
                ),
                "Cafe" to listOf(
                    "Cafe",
                    "Internet cafe"
                ),
                "Nature" to listOf(
                    "Nature",
                    "Nature reserve",
                ),
                "Adult" to listOf(
                    "Stripclub",
                    "Brothel"
                ),
                "Swimming" to listOf(
                    "Swimming pool",
                    "Public bath",
                    "Bathing place",
                    "Water park"
                ),
                "Waste" to listOf(
                    "Waste basket",
                    "Waste disposal"
                ),
                "Drinking water" to listOf(
                    "Drinking water",
                    "Water point"
                )
            )

        for (map in unifyCategoryMap) {
            if (map.value.contains(categoryName)) return map.key
        }

        return categoryName
    }

    fun extractPropImagesFromTags(tags: Map<String, String>): List<Int> {
        val imageList = mutableListOf<Int>()

        if (tags["internet_access"] == "wlan" || tags["internet_access"] == "yes") imageList.add(R.drawable.wifi)
        if (tags["fuel"] == "yes") imageList.add(R.drawable.fuel_station)
        if (tags["smoking"] == "yes") imageList.add(R.drawable.smoking)
        if (tags["smoking"] == "no") imageList.add(R.drawable.smoking_prohibited)
        if (tags["wheelchair"] == "yes") imageList.add(R.drawable.wheelchair)
        if (tags["toilets"] == "yes") imageList.add(R.drawable.toilets)

        return imageList
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

    // map of drawables for each unified category
    private val poiCategoryDrawableMap = mapOf(
        R.string.art to R.drawable.arts_centre,
        R.string.animals to R.drawable.animal,
        R.string.accommodation to R.drawable.accommodation,
        R.string.finance to R.drawable.money_icon,
        R.string.bench to R.drawable.bench,
        R.string.drink to R.drawable.bar,
        R.string.camping to R.drawable.camping,
        R.string.nautical to R.drawable.anchor,
        R.string.parking to R.drawable.parking,
        R.string.food to R.drawable.restaurant,
        R.string.study to R.drawable.study,
        R.string.library to R.drawable.library,
        R.string.public_transport to R.drawable.bus_station,
        R.string.tourism to R.drawable.tourist,
        R.string.swimming to R.drawable.swimming_pool,
        R.string.sport to R.drawable.fitness,
        R.string.health to R.drawable.hospital,
        R.string.shop to R.drawable.shop,
        R.string.leisure to R.drawable.leisure,
        R.string.cafe to R.drawable.cafe,
        R.string.gambling to R.drawable.casino,
        R.string.waste to R.drawable.waste_basket,
        R.string.traffic to R.drawable.traffic,
        R.string.water to R.drawable.drinking_water,
        R.string.hunting to R.drawable.hunting_stand,
        R.string.nightclub to R.drawable.nightclub,
        R.string.car_services to R.drawable.car,
        R.string.bicycle_services to R.drawable.bicycle,
        R.string.private_transport to R.drawable.taxi,
        R.string.nature to R.drawable.nature,
        R.string.public_facility to R.drawable.crowd,
        R.string.live_music to R.drawable.music_live,
        R.string.care to R.drawable.care,
        R.string.relax to R.drawable.relax,
        R.string.government_facilities to R.drawable.government_facility,
        R.string.mail_and_shipping to R.drawable.post_box,
        R.string.emergency to R.drawable.emergency,
        R.string.worship_and_memorial_grounds to R.drawable.place_of_worship,
        R.string.storage to R.drawable.storage,
        R.string.services to R.drawable.services,
        R.string.administration to R.drawable.administration,
        R.string.museum to R.drawable.museum,
        R.string.cinema to R.drawable.cinema,
        R.string.theatre to R.drawable.theatre,
        R.string.charging_station to R.drawable.charging_station,
        R.string.fuel_station to R.drawable.fuel_station,
        R.string.adult to R.drawable.adult,
        R.string.boat_services to R.drawable.boat_rent,
        R.string.information to R.drawable.information,
        R.string.ice_cream to R.drawable.ice_cream,
        R.string.vending_machine to R.drawable.vending_machine,
        R.string.farm to R.drawable.farm,
        R.string.other to R.drawable.question_mark_24
    )

    fun getDrawableForUnifiedPoiCategory(categoryName: String, context: Context): Int? {
        val stringResId = poiCategoryDrawableMap.keys.find { context.getString(it) == categoryName }
        return stringResId?.let { poiCategoryDrawableMap[it] }
    }

    private var unifyCategoryMap = mapOf(
        R.string.art to listOf(
            "Arts centre",
            "Artwork",
            "Gallery",
            "Art",
            "Art gallery"
        ),
        R.string.animals to listOf(
            "Animal boarding",
            "Animal breeding",
            "Animal shelter",
            "Animal training",
            "Wildlife feeding",
            "Bird hide",
            "Feeding rack",
            "Stables",
            "Feeding place",
            "Watering place",
            "Game feeding",
            "Zoo",
            "Fodder rack",
            "Dog toilet",
            "Hayloft",
            "Hay loft",
            "Trail riding station"
        ),
        R.string.accommodation to listOf(
            "Apartment",
            "Hostel",
            "Hotel",
            "Love hotel",
            "Chalet",
            "Guest house",
            "Motel",
            "Wilderness hut",
            "Alpine hut"
        ),
        R.string.finance to listOf(
            "Atm",
            "Bank",
            "Bureau de change",
            "Money transfer",
            "Payment centre",
            "Payment terminal"
        ),
        R.string.drink to listOf(
            "Bar",
            "Pub",
            "Biergarten",
            "Bar, casino",
            "Juice bar",
            "Wine bar"
        ),
        R.string.camping to listOf(
            "Camp site",
            "Caravan site",
            "Firepit",
            "Kitchen",
            "Camp pitch"
        ),
        R.string.nautical to listOf(
            "Marina",
            "Mooring",
            "Ferry terminal",
            "Wreck",
            "Bridge",
            "Gate",
            "Slipway",
            "Anchorage",
            "Anchor berth",
            "Beacon cardinal",
            "Beacon isolated danger",
            "Beacon lateral",
            "Beacon safe water",
            "Beacon special purpose",
            "Berth",
            "Bunker station",
            "Building",
            "Buoy cardinal",
            "Buoy installation",
            "Buoy isolated danger",
            "Buoy lateral",
            "Buoy safe water",
            "Buoy special purpose",
            "Cable area",
            "Cable overhead",
            "Cable submarine",
            "Causeway",
            "Checkpoint",
            "Coastguard station",
            "Control point",
            "Crane",
            "Distance mark",
            "Dredged area",
            "Dumping ground",
            "Fairway",
            "Ferry route",
            "Harbour basin",
            "Harbour",
            "Hulk",
            "Landmark",
            "Light major",
            "Light minor",
            "Light float",
            "Light vessel",
            "Lock basin",
            "Marine farm",
            "Military area",
            "Navigation line",
            "Notice",
            "Obstruction",
            "Platform",
            "Production area",
            "Pilot boarding",
            "Pile",
            "Pipeline area",
            "Pipeline overhead",
            "Pipeline submarine",
            "Precautionary area",
            "Pylon",
            "Radar reflector",
            "Radar transponder",
            "Radar station",
            "Calling-in point",
            "Radio station",
            "Recommended track",
            "Rescue station",
            "Restricted area",
            "Seaplane landing area",
            "Shoreline construction",
            "Signal station traffic",
            "Signal station warning",
            "Small craft facility",
            "Separation boundary",
            "Separation crossing",
            "Separation lane",
            "Separation line",
            "Separation roundabout",
            "Separation zone",
            "Turning basin",
            "Two-way route",
            "Rock",
            "Virtual aton",
            "Vehicle transfer",
            "Waterway gauge"
        ),
        R.string.parking to listOf(
            "Motorcycle parking",
            "Parking",
            "Bicycle parking",
            "Parking entrance",
            "Parking space",
            "Parking exit",
            "Parking section"
        ),
        R.string.food to listOf(
            "Restaurant",
            "Fast food",
            "Food court",
            "Bbq",
            "Canteen"
        ),
        R.string.study to listOf(
            "School",
            "Sailing school",
            "University",
            "College",
            "Driving school",
            "Kindergarten",
            "Language school",
            "Training",
            "Music school",
            "Traffic park",
            "Research institute",
            "Surf school",
            "First aid school",
            "Dancing school",
            "Art school",
            "Cooking school",
            "Flight school",
            "Prep school"
        ),
        R.string.library to listOf(
            "Library",
            "Public bookcase",
            "Library dropoff"
        ),
        R.string.public_transport to listOf(
            "Train stop",
            "Tram",
            "Trolley bay",
            "Tram stop",
            "Train disused",
            "Train halt",
            "Train station",
            "Trolleybus",
            "Bus",
            "Bus station",
            "Bus stop",
            "Bus stop & tram stop",
            "Bus stop & trolleybus stop",
            "Ferry terminal",
            "Yard",
            "Halt",
            "Platform",
            "Stop",
            "Stop position",
            "Station",
            "Lounge",
            "Destination display",
            "Tram stop & bus stop",
            "Subway entrance"
        ),
        R.string.swimming to listOf(
            "Water park",
            "Bathing place",
            "Public bath",
            "Swimming pool",
            "Swimming",
            "Swimming area"
        ),
        R.string.sport to listOf(
            "Sport",
            "Fitness station",
            "Sports centre",
            "Fitness centre",
            "Pitch",
            "Golf course",
            "Miniature golf",
            "Trampoline park",
            "Ice rink",
            "Dojo",
            "Sports hall",
            "Badminton"
        ),
        R.string.health to listOf(
            "Pharmacy",
            "Dentist",
            "Veterinary",
            "Clinic",
            "Doctors",
            "First aid",
            "Hospital",
            "Health"
        ),
        R.string.shop to listOf(
            "Marketplace",
            "Shop",
            "Studio",
            "Psychic",
            "Furniture rental"
        ),
        R.string.leisure to listOf(
            "Leisure",
            "Bowling alley",
            "Dance",
            "Disc golf course",
            "Escape game",
            "Adult gaming centre",
            "Bandstand",
            "Bleachers",
            "Fishing",
            "Beach resort",
            "Horse riding",
            "Hackerspace",
            "Playground",
            "Planetarium",
            "Outdoor seating",
            "Community centre",
            "Dive centre",
            "Exhibition centre",
            "Conference centre",
            "Stage",
            "Toy library",
            "Amusement arcade",
            "Common",
            "Hookah lounge",
            "Quest"
        ),
        R.string.cafe to listOf(
            "Internet cafe",
            "Cafe"
        ),
        R.string.gambling to listOf(
            "Casino",
            "Bar, casino",
            "Gambling"
        ),
        R.string.waste to listOf(
            "Waste disposal",
            "Waste basket",
            "Recycling",
            "Waste transfer station",
            "Sanitary dump station",
            "Waste container"
        ),
        R.string.water to listOf(
            "Water point",
            "Drinking water"
        ),
        R.string.traffic to listOf(
            "Traffic mirror",
            "Weighbridge",
            "Speed enforcement",
            "Grit bin"
        ),
        R.string.tourism to listOf(
            "Tourism",
            "Theme park",
            "Zoo",
            "Photo booth",
            "Street lamp",
            "Wine cellar",
            "Miniature",
            "Attraction",
            "Viewpoint",
            "Binoculars",
            "Bell",
            "Fountain",
            "Aquarium",
            "Museum",
            "Clock",
            "Memorial"
        ),
        R.string.car_services to listOf(
            "Vehicle inspection",
            "Car rental",
            "Car wash",
            "Vacuum cleaner",
            "Vehicle ramp",
            "Compressed air",
            "Car sharing",
            "Driver training",
            "Car pooling",
            "Caravan rental"
        ),
        R.string.bicycle_services to listOf(
            "Bicycle wash",
            "Bicycle repair station",
            "Bicycle rental",
            "Compressed air"
        ),
        R.string.boat_services to listOf(
            "Boat rental",
            "Boat sharing",
            "Boat storage"
        ),
        R.string.private_transport to listOf(
            "Taxi"
        ),
        R.string.storage to listOf(
            "Left luggage",
            "Locker",
            "Luggage locker"
        ),
        R.string.nature to listOf(
            "Nature",
            "Nature reserve",
            "Park",
            "Dog park",
            "Garden"
        ),
        R.string.public_facility to listOf(
            "Table",
            "Shower",
            "Shelter",
            "Lean to",
            "Dressing room",
            "Lounger",
            "Picnic site",
            "Picnic table",
            "Toilets",
            "Baking oven",
            "Telephone",
            "Give box",
            "Events venue",
            "Event venue",
            "Social centre",
            "Wedding venue",
            "Public building"
        ),
        R.string.hunting to listOf(
            "Hunting stand",
            "Hunting lodge"
        ),
        R.string.live_music to listOf(
            "Music venue",
            "Concert hall"
        ),
        R.string.care to listOf(
            "Nursing home",
            "Childcare",
            "Baby hatch",
            "Social facility"
        ),
        R.string.relax to listOf(
            "Relax room",
            "Tanning salon",
            "Sauna",
            "Kneipp water cure",
            "Spa"
        ),
        R.string.government_facilities to listOf(
            "Courthouse",
            "Prison",
            "Townhall"
        ),
        R.string.mail_and_shipping to listOf(
            "Post box",
            "Post depot",
            "Parcel locker",
            "Post office",
            "Loading dock",
            "Letter box",
            "Mailroom"
        ),
        R.string.emergency to listOf(
            "Police",
            "Hospital",
            "Fire station"
        ),
        R.string.worship_and_memorial_grounds to listOf(
            "Place of worship",
            "Grave yard",
            "Funeral hall",
            "Place of mourning",
            "Mortuary",
            "Monastery",
            "Crematorium"
        ),
        R.string.services to listOf(
            "Reception desk",
            "Ranger station",
            "Service station",
            "Lost property office"
        ),
        R.string.adult to listOf(
            "Brothel",
            "Stripclub",
            "Swingerclub"
        ),
        R.string.fuel_station to listOf(
            "Fuel"
        ),
        R.string.ice_cream to listOf(
            "Ice cream"
        ),
        R.string.information to listOf(
            "Information"
        ),
        R.string.vending_machine to listOf(
            "Vending machine",
            "Ticket validator"
        ),
        R.string.farm to listOf(
            "Farm"
        ),
        R.string.cinema to listOf(
            "Cinema"
        ),
        R.string.charging_station to listOf(
            "Charging station"
        ),
        R.string.theatre to listOf(
            "Theatre"
        ),
        R.string.nightclub to listOf(
            "Nightclub"
        ),
        R.string.bench to listOf(
            "Bench"
        ),
        R.string.other to emptyList()
    )

    fun getUnifiedPoiCategory(categoryName: String): Int {
        for ((key, value) in unifyCategoryMap) {
            if (value.contains(categoryName)) return key
        }
        unifyCategoryMap = unifyCategoryMap.toMutableMap().apply {
            replace(
                R.string.other,
                unifyCategoryMap[R.string.other]?.plus(categoryName) ?: listOf(categoryName)
            )
        }
        return R.string.other
    }

    fun getCategoriesUnderUnifiedCategory(@StringRes categoryName: Int): List<String>? =
        unifyCategoryMap[categoryName]
}
