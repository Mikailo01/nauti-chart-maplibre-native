package com.bytecause.map.util

import android.graphics.Color
import org.maplibre.android.style.layers.Property.ICON_ANCHOR_BOTTOM
import org.maplibre.android.style.layers.Property.ICON_ANCHOR_CENTER

object MapFragmentConstants {
    const val DEFAULT_BUTTON_STATE = 0
    const val TRACKING_BUTTON_STATE = 1

    const val ANCHORAGE_RADIUS_MOVE_BY = 0.00001

    const val ZOOM_IN_DEFAULT_LEVEL = 17.0
    const val SYMBOL_ICON_SIZE = 1f
    const val PIN_ICON = "pin_icon"
    const val MAP_MARKER = "map_marker"
    const val LINE_WIDTH = 2f

    const val MEASURE_LINE_GEOJSON_SOURCE = "measure_line_geojson_source"
    const val MEASURE_LINE_LAYER = "measure_line_layer"

    const val SYMBOL_ICON_ANCHOR_BOTTOM = ICON_ANCHOR_BOTTOM
    const val SYMBOL_ICON_ANCHOR_CENTER = ICON_ANCHOR_CENTER

    const val PULSING_CIRCLE_GEOJSON_SOURCE = "pulsing-circle-geojson-source"
    const val PULSING_CIRCLE_LAYER = "pulsing-circle-layer"
    const val PULSING_CIRCLE_ANIMATION_DURATION = 1500L
    const val ANIMATED_CIRCLE_RADIUS = 15f
    const val ANIMATED_CIRCLE_COLOR = Color.DKGRAY

    const val ANCHORAGE_BORDER_RADIUS_GEOJSON_SOURCE = "anchorage_border_radius_geojson_source"
    const val ANCHORAGE_BORDER_RADIUS_LAYER = "anchorage_border_radius_layer"
    const val ANCHORAGE_RADIUS_CENTER_SYMBOL_GEOJSON_SOURCE =
        "anchorage_radius_center_symbol_geojson_source"
    const val ANCHORAGE_RADIUS_CENTER_SYMBOL_LAYER = "anchorage_radius_center_symbol_layer"
    const val ANCHORAGE_CENTER_SYMBOL_ICON = "anchorage_center_symbol_icon"
    const val ANIMATE_TO_RADIUS_BOUNDS_PADDING = 150

    const val ANCHOR_CHAIN_LINE_GEOJSON_SOURCE = "anchor_chain_line_geojson_source"
    const val ANCHOR_CHAIN_LINE_LAYER = "anchor_chain_line_layer"

    const val ANCHORAGE_MOVEMENT_LINE_GEOJSON_SOURCE = "anchorage_movement_line_geojson_source"
    const val ANCHORAGE_MOVEMENT_LINE_LAYER = "anchorage_movement_line_layer"

    const val SYMBOL_TYPE = "symbol-type"

    const val POI_GEOJSON_SOURCE = "poi-geojson-source"
    const val POI_SYMBOL_LAYER = "poi-geojson-layer"
    const val POI_SYMBOL_ICON_SIZE = 1.3f
    const val POI_CATEGORY_KEY = "poi_category"
    const val POI_SYMBOL_PROPERTY_ID_KEY = "poi_id"
    const val POI_SYMBOL_NAME_KEY = "poi_symbol_name"
    const val POI_SYMBOL_TEXT_OFFSET_KEY = "poi_symbol_text_offset"

    const val VESSEL_SYMBOL_PROPERTY_ID_KEY = "vessel_id"
    const val VESSEL_GEOJSON_SOURCE = "vessel-geojson-source"
    const val VESSEL_SYMBOL_LAYER = "vessel-geojson-layer"
    const val VESSEL_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY = "vessel_icon_drawable"
    const val VESSEL_SYMBOL_PROPERTY_SELECTED_KEY = "vessel_selected"
    const val VESSEL_SYMBOL_ICON_ROTATION_KEY = "vessel_icon_rotation"
    const val VESSEL_SYMBOL_ICON_DRAWABLE_KEY_PREFIX = "vessel_icon_"
    const val VESSEL_SYMBOL_SELECTED_SIZE = 1.2f
    const val VESSEL_SYMBOL_DEFAULT_SIZE = 1f

    const val HARBOUR_SYMBOL_PROPERTY_ID_KEY = "harbour_id"
    const val HARBOUR_GEOJSON_SOURCE = "harbour-geojson-source"
    const val HARBOUR_SYMBOL_LAYER = "harbour-geojson-layer"
    const val HARBOUR_SYMBOL_PROPERTY_SELECTED_KEY = "harbour_selected"
    const val HARBOUR_ICON = "harbour_icon"
    const val HARBOUR_SYMBOL_SELECTED_SIZE = 1.2f
    const val HARBOUR_SYMBOL_DEFAULT_SIZE = 1f

    const val ANCHORAGES_GEOJSON_SOURCE = "anchorages-geojson-source"
    const val ANCHORAGES_SYMBOL_LAYER = "anchorages-symbol-layer"
    const val ANCHORAGE_ICON = "anchorage-icon"
    const val ANCHORAGE_SYMBOL_DEFAULT_SIZE = 0.6f

    const val CUSTOM_POI_GEOJSON_SOURCE = "custom-poi-geojson-source"
    const val CUSTOM_POI_SYMBOL_LAYER = "custom-poi-symbol-layer"
    const val CUSTOM_POI_SYMBOL_DEFAULT_SIZE = 0.6f
    const val CUSTOM_POI_SYMBOL_SELECTED_SIZE = 0.7f
    const val CUSTOM_POI_SYMBOL_PROPERTY_ID_KEY = "custom_poi_id"
    const val CUSTOM_POI_SYMBOL_PROPERTY_SELECTED_KEY = "custom_poi_selected"
    const val CUSTOM_POI_SYMBOL_ICON_DRAWABLE_PROPERTY_KEY = "vessel_icon_drawable"
    const val CUSTOM_POI_SYMBOL_ICON_DRAWABLE_KEY_PREFIX = "custom_poi_icon_"

    const val POIS_VISIBILITY_ZOOM_LEVEL = 10.0
}