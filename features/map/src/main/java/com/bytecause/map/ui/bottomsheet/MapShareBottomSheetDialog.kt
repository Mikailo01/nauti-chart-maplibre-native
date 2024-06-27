package com.bytecause.map.ui.bottomsheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bytecause.feature.map.R
import com.bytecause.feature.map.databinding.ShareBottomSheetFragmentBinding
import com.bytecause.map.util.MapUtil
import com.bytecause.util.delegates.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.maplibre.android.geometry.LatLng

class MapShareBottomSheetDialog : BottomSheetDialogFragment(R.layout.share_bottom_sheet_fragment) {

    private val binding by viewBinding(ShareBottomSheetFragmentBinding::bind)

    private val args: MapShareBottomSheetDialogArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        val geoPoint = LatLng(args.geoPoint[0].toDouble(), args.geoPoint[1].toDouble())

        binding.sendTextview.setOnClickListener {
            // Implicit intent
            val geoInfo = getString(com.bytecause.core.resources.R.string.implicit_geo_info_arg).format(
                MapUtil.latitudeToDMS(geoPoint.latitude),
                MapUtil.longitudeToDMS(geoPoint.longitude)
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, geoInfo)

            startActivity(
                Intent.createChooser(
                    intent,
                    getString(com.bytecause.core.resources.R.string.implicit_geo_intent_title)
                )
            )
        }

        binding.copyCoordinatesTextview.setOnClickListener {
            val myClipboard: ClipboardManager =
                activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val myClip: ClipData = ClipData.newPlainText(
                "GeoPoint",
                MapUtil.latitudeToDMS(geoPoint.latitude) + " " + MapUtil.longitudeToDMS(geoPoint.longitude)
            )
            myClipboard.setPrimaryClip(myClip)

            Toast.makeText(context, getString(com.bytecause.core.resources.R.string.copied), Toast.LENGTH_SHORT).show()
        }

        binding.thirdPartyAppTextview.setOnClickListener {
            // Implicit intent
            val geoLocation = Uri.parse("geo:" + geoPoint.latitude + ", " + geoPoint.longitude)
            val intent = Intent(Intent.ACTION_VIEW, geoLocation)

            startActivity(
                Intent.createChooser(
                    intent,
                    getString(com.bytecause.core.resources.R.string.implicit_geo_intent_title)
                )
            )
        }
    }
}