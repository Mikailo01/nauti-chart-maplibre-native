package com.bytecause.nautichart.ui.view.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bytecause.nautichart.R
import com.bytecause.nautichart.ui.view.custom.CustomMapView

class CustomCompassView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var customMapView: CustomMapView? = null

    private val compassForegroundBitmap =
        ContextCompat.getDrawable(context, R.drawable.compass_foreground)?.toBitmap()
    private val mCompassMatrix = Matrix()
    private val sSmoothPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    private val paint = Paint().apply {
        color = Color.WHITE
    }

    fun passMapView(customMapView: CustomMapView) {
        this.customMapView = customMapView
    }

    private var centerX = 30
    private var centerY = 30

    private val layoutWidth = compassForegroundBitmap?.width?.plus(30)
    private val layoutHeight = compassForegroundBitmap?.height?.plus(30)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        customMapView?.let {
            if (compassForegroundBitmap == null || layoutHeight == null || layoutWidth == null) return

            val proj = it.projection

            // Set the new positions
            val parentView = parent as ViewGroup
            val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.apply {
                leftMargin = centerX
                topMargin = centerY
                height = layoutHeight
                width = layoutWidth
            }
            parentView.updateViewLayout(this, layoutParams)

            // Calculate the center of the circle
            val circleCenterX = layoutParams.width / 2
            val circleCenterY = layoutParams.height / 2

            // Draw the circle
            canvas.drawCircle(
                circleCenterX.toFloat(),
                circleCenterY.toFloat(),
                layoutParams.width / 2f,
                paint
            )

            // Calculate the coordinates to draw the bitmap at the center of the circle
            val bitmapX = circleCenterX - compassForegroundBitmap.width / 2
            val bitmapY = circleCenterY - compassForegroundBitmap.height / 2

            mCompassMatrix.setRotate(
                proj.orientation,
                circleCenterX.toFloat(),
                circleCenterY.toFloat()
            )

            canvas.apply {
                concat(mCompassMatrix)
                // Draw the bitmap at the calculated coordinates
                drawBitmap(
                    compassForegroundBitmap,
                    bitmapX.toFloat(),
                    bitmapY.toFloat(),
                    sSmoothPaint
                )
            }
        }
    }

    fun setCompassCenterXY(x: Int, y: Int) {
        this.centerX = x
        this.centerY = y
    }
}