package com.bytecause.nautichart.ui.view.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import org.osmdroid.views.MapView

class VesselRadiusMarkerClusterer(context: Context) : CustomRadiusMarkerClusterer(context) {

    private val density = context.resources.displayMetrics.density

    // Creates circle graph clusterer.
    override fun buildClusterMarker(
        cluster: CustomStaticCluster?,
        mapView: MapView?
    ): CustomMarker? {
        cluster ?: return null
        mapView ?: return null

        val colorList = mutableListOf<Int>()
        val m = CustomMarker(mapView)
        m.position = cluster.getPosition()
        m.setInfoWindow(null)
        m.setAnchor(mAnchorU, mAnchorV)

        // Iterate over markers in cluster to fetch all icons drawable's colors.
        cluster.getItems().forEach {
            it.drawableColor.let { color ->
                color ?: return@let
                colorList.add(color)
            }
        }

        drawCircle(colorList).let { bitmap ->
            val iconCanvas = Canvas(bitmap)
            iconCanvas.drawBitmap(bitmap, 0f, 0f, null)
            val text = cluster.getSize().toString()
            mTextPaint?.let { textPaint ->
                val textHeight: Int = (textPaint.descent() + textPaint.ascent()).toInt()
                iconCanvas.drawText(
                    text,
                    mTextAnchorU * bitmap.width,
                    mTextAnchorV * bitmap.height - textHeight / 2,
                    textPaint
                )
            }
            m.icon = BitmapDrawable(mapView.context.resources, bitmap)
        }
        return m
    }

    private fun drawCircle(colorList: List<Int>): Bitmap {
        val radius = if (colorList.size < 1000) 12f * density else 12f * 1.4f * density
        val bitmap = Bitmap.createBitmap(
            (radius * 2).toInt(),
            (radius * 2).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            strokeWidth = 3f
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        // if colorList contains more than 1 color, divide 360 by the size of the list, which will
        // creates circle graph representing how many vessel's types are in corresponding area.
        val degree = if (colorList.size > 1) {
            360f / colorList.size
        } else {
            360f
        }

        var currentAngle = 0f

        // sort colorList to make circle graph clusterer consistent.
        for (color in colorList.sorted()) {
            paint.color = color
            canvas.drawArc(rect, currentAngle, degree, true, paint)
            currentAngle += degree
        }
        return bitmap
    }
}