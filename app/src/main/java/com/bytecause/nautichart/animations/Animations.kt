package com.bytecause.nautichart.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.animation.addListener
import com.bytecause.nautichart.R
import com.bytecause.nautichart.ui.view.custom.CustomMapView


class SmoothMapOrientationAnimator(private val customMapView: CustomMapView) {

    private val duration = 300L // Animation duration in milliseconds
    private val startOrientation = customMapView.mapOrientation

    private var animator: ValueAnimator? = null

    fun startAnimation(targetOrientation: Float) {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(
            startOrientation,
            when {
                startOrientation > 0 && (targetOrientation - startOrientation) > 180 -> targetOrientation - 360
                startOrientation < 0 && (-targetOrientation + -startOrientation) > -180 -> -targetOrientation
                startOrientation < 0 && (-targetOrientation + -startOrientation) < - 180 -> targetOrientation - 360
                else -> targetOrientation
            }
        )
        animator?.apply {
            this.duration = this@SmoothMapOrientationAnimator.duration

            addUpdateListener { valueAnimator ->
                val interpolatedOrientation = valueAnimator.animatedValue as Float
                customMapView.mapOrientation = interpolatedOrientation
            }
            start()
        }
    }

    fun cancelAnimation() {
        animator?.cancel()
    }

    fun isAnimationStarted(): Boolean {
        return if (animator != null) {
            animator!!.isStarted
        } else false
    }
}

class ViewClickAnimation() {

    fun animateClick(itemView: View, duration: Long, initColor: Int, finalColor: Int) {
        val scaleX = ObjectAnimator.ofFloat(itemView, View.SCALE_X, 0.6f, 1f)
        val scaleY = ObjectAnimator.ofFloat(itemView, View.SCALE_Y, 0.6f, 1f)
        scaleX.duration = duration
        scaleY.duration = duration

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)

        // Create an ObjectAnimator for background color property
        val colorAnimator = ObjectAnimator.ofArgb(itemView.background, null, initColor, finalColor)
        colorAnimator.duration = duration

        colorAnimator.addUpdateListener { animator ->
            val newColor = animator.animatedValue as Int
            itemView.setBackgroundResource(R.drawable.rounded_background)

            val background = itemView.background
            if (background is GradientDrawable) {
                // Apply tint color to the background
                background.setColor(newColor)
            }
        }

        colorAnimator.addListener(
            onEnd = { },
            onCancel = {}
        )

        animatorSet.start()
        colorAnimator.start()
    }
}