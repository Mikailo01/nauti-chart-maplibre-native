package com.bytecause.nautichart.interfaces

import android.view.View
import android.widget.CompoundButton
import com.bytecause.nautichart.ui.view.overlay.CustomMarker
import org.osmdroid.util.GeoPoint

interface SelectPoiMarkerIconInterface {

    fun onIconClickListener(view: View, position: Int)
}

interface MapFragmentInterface {

    fun openMarkerBottomSheet(marker: CustomMarker)

    fun markerToMapAdded(geoPoint: GeoPoint)

    fun updateDistanceTextView(text: String)

    fun overlayAddedListener()
}

interface SearchHistoryRecyclerViewInterface {

    fun onItemViewClickListener(view: View, parentIndex: Int, childIndex: Int)
}

interface SelectLayerInterface {

    fun onItemViewClickListener(view: View, position: Int)
    fun onItemViewLongClickListener(view: View, position: Int)
}

interface SelectCheckBoxInterface {

    fun onCheckBoxClickListener(buttonView: CompoundButton, position: Int, parentPosition: Int, isChecked: Boolean)
}

interface CountryAndRegionsListenerInterface {

    fun onCheckBoxClickListener(buttonView: CompoundButton, position: Int, parentPosition: Int, isChecked: Boolean)
    fun onExpandClickListener(position: Int)
    fun onCountryDownloadClickListener(position: Int)
}

interface RecyclerViewOnClickListener {

    fun onItemViewClickListener(view: View, position: Int)
}
