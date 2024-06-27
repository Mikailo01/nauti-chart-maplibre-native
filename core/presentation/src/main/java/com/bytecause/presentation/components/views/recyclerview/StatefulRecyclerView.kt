package com.bytecause.presentation.components.views.recyclerview

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.bytecause.util.extensions.parcelable

/**
 * Class {@link StatefulRecyclerView} extends {@link RecyclerView} and adds position management on configuration changes.
 *
 * @author FrantisekGazo
 * @version 2016-03-15
 */
class StatefulRecyclerView : RecyclerView {
    private var mLayoutManagerSavedState: Parcelable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    )

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState())
        bundle.putParcelable(
            SAVED_LAYOUT_MANAGER, this.layoutManager?.onSaveInstanceState()
        )
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var newState: Parcelable? = state
        if (newState is Bundle) {
            val bundle = newState
            mLayoutManagerSavedState = bundle.parcelable(SAVED_LAYOUT_MANAGER)
            newState = bundle.parcelable(SAVED_SUPER_STATE)
        }
        super.onRestoreInstanceState(newState)
    }

    /**
     * Restores scroll position after configuration change.
     *
     *
     * **NOTE:** Must be called after adapter has been set.
     */
    private fun restorePosition() {
        if (mLayoutManagerSavedState != null) {
            this.layoutManager!!.onRestoreInstanceState(mLayoutManagerSavedState)
            mLayoutManagerSavedState = null
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        restorePosition()
    }

    companion object {
        private const val SAVED_SUPER_STATE = "super-state"
        private const val SAVED_LAYOUT_MANAGER = "layout-manager-state"
    }
}
