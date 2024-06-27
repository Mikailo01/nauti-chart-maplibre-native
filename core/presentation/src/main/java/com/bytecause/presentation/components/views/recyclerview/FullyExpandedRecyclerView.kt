package com.bytecause.presentation.components.views.recyclerview


import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class FullyExpandedRecyclerView: RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val newHeightSpec = MeasureSpec.makeMeasureSpec(
            Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST
        )
        super.onMeasure(widthSpec, newHeightSpec)
    }
}