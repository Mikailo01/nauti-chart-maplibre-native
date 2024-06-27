package com.bytecause.presentation.components.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText

class CustomTextInputEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var drawableClickListener: OnDrawableClickListener? = null
    private var textChangedListener: OnTextChangedListener? = null

    private val _drawableList = mutableListOf<Drawable>()
    val drawableList get() = _drawableList.toList()

    private var leftDrawable: Drawable? = null
    private var rightDrawable: Drawable? = null

    interface OnDrawableClickListener {
        fun onStartDrawableClick(view: CustomTextInputEditText)
        fun onEndDrawableClick(view: CustomTextInputEditText)
    }

    interface OnTextChangedListener {
        fun onTextChanged(text: CharSequence?)
    }

    fun setOnStartDrawableClickListener(listener: OnDrawableClickListener) {
        this.drawableClickListener = listener
    }

    fun setOnTextChangedListener(listener: OnTextChangedListener) {
        this.textChangedListener = listener
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        textChangedListener?.onTextChanged(text)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val startDrawableBounds: Rect
        val endDrawableBounds: Rect
        if (event.action == MotionEvent.ACTION_UP && drawableClickListener != null) {
            val x = event.x.toInt()
            val y = event.y.toInt()

            val startDrawable = leftDrawable
            val endDrawable = rightDrawable

            startDrawableBounds = startDrawable?.bounds ?: leftDrawable?.bounds ?: Rect()
            endDrawableBounds = endDrawable?.bounds ?: Rect()

            val startDrawableAbsoluteBounds = Rect(
                left + startDrawableBounds.left,
                top,
                left + startDrawableBounds.right + startDrawableBounds.right / 2,
                bottom
            )

            val endDrawableAbsoluteBounds = Rect(
                right - (endDrawableBounds.right + endDrawableBounds.right / 2),
                top,
                right - endDrawableBounds.right / 2,
                bottom
            )

            // Check if the touch event is within the bounds of the drawable.
            if (startDrawableAbsoluteBounds.contains(x, y)) {
                drawableClickListener?.onStartDrawableClick(this)
                return true
            }
            if (endDrawableAbsoluteBounds.contains(x, y)) {
                drawableClickListener?.onEndDrawableClick(this)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setDrawables(
        left: Drawable? = leftDrawable,
        right: Drawable? = rightDrawable,
        top: Drawable? = null,
        bottom: Drawable? = null
    ) {
        left?.setBounds(0, 0, left.intrinsicWidth, left.intrinsicHeight)
        right?.setBounds(0, 0, right.intrinsicWidth, right.intrinsicHeight)
        top?.setBounds(0, 0, top.intrinsicWidth, top.intrinsicHeight)
        bottom?.setBounds(0, 0, bottom.intrinsicWidth, bottom.intrinsicHeight)

        left?.bounds?.let leftLet@{ leftBound ->
            right?.bounds?.let { rightBound ->
                if (leftBound == rightBound) return@leftLet
                right.setBounds(0, 0, leftBound.right, leftBound.bottom)
            }
        }

        left?.let {
            if (left == leftDrawable) return@let
            leftDrawable = it
            if (!drawableList.contains(leftDrawable)) _drawableList.add(it)
        } ?: run {
            leftDrawable = null
        }

        right?.let {
            if (right == rightDrawable) return@let
            rightDrawable = it
            if (!drawableList.contains(rightDrawable)) _drawableList.add(it)
        } ?: run {
            rightDrawable = null
        }

        setCompoundDrawables(left, top, right, bottom)
    }
}