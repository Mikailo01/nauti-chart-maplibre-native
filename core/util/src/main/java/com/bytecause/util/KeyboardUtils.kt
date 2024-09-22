package com.bytecause.util

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager

class KeyboardUtils(
    act: Activity,
    listener: SoftKeyboardToggleListener
) : OnGlobalLayoutListener {

    private var mRootView: View? = null
    private var mCallback: SoftKeyboardToggleListener? = null
    private var prevValue: Boolean? = null
    private var mScreenDensity = 0f

    companion object {
        private const val MAGIC_NUMBER: Int = 200
        private val sListenerMap: HashMap<SoftKeyboardToggleListener, KeyboardUtils> = HashMap()

        fun interface SoftKeyboardToggleListener {
            fun onToggleSoftKeyboard(isVisible: Boolean)
        }

        /**
         * Add a new keyboard listener
         * @param act calling activity
         * @param listener callback
         */
        fun addKeyboardToggleListener(act: Activity, listener: SoftKeyboardToggleListener) {
            removeKeyboardToggleListener(listener)
            sListenerMap[listener] = KeyboardUtils(act, listener)
        }

        /**
         * Remove a registered listener
         * @param listener [SoftKeyboardToggleListener]
         */
        fun removeKeyboardToggleListener(listener: SoftKeyboardToggleListener?) {
            if (sListenerMap.containsKey(listener)) {
                sListenerMap[listener]?.removeListener()
                sListenerMap.remove(listener)
            }
        }

        /**
         * Force closes the soft keyboard
         * @param activeView the view with the keyboard focus
         */
        fun forceCloseKeyboard(activeView: View) {
            val inputMethodManager =
                activeView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activeView.windowToken, 0)
        }

        /**
         * Remove all registered keyboard listeners
         */
        fun removeAllKeyboardToggleListeners() {
            for (l in sListenerMap.keys) requireNotNull(sListenerMap[l]).removeListener()
            sListenerMap.clear()
        }

        /**
         * Manually toggle soft keyboard visibility
         * @param context calling context
         * @param view InputMethodManager's target view
         */
        fun toggleKeyboardVisibility(context: Context, view: View) {
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, 0)
        }
    }

    init {
        mCallback = listener

        mRootView =
            (act.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0).apply {
                viewTreeObserver.addOnGlobalLayoutListener(this@KeyboardUtils)
            }

        mScreenDensity = act.resources.displayMetrics.density
    }

    override fun onGlobalLayout() {
        val r = Rect()
        mRootView?.getWindowVisibleDisplayFrame(r)

        val heightDiff = mRootView!!.rootView.height - (r.bottom - r.top)
        val dp = heightDiff / mScreenDensity
        val isVisible: Boolean = dp > MAGIC_NUMBER

        if (mCallback != null && (prevValue == null || isVisible != prevValue)) {
            prevValue = isVisible
            mCallback?.onToggleSoftKeyboard(isVisible)
        }
    }

    private fun removeListener() {
        mCallback = null
        mRootView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
    }
}