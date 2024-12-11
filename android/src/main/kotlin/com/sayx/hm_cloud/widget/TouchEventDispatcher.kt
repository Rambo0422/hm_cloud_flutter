package com.sayx.hm_cloud.widget

import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.LogUtils

object TouchEventDispatcher {

    val views: MutableList<View> = mutableListOf()

    fun registerView(view: View) {
        if (!views.contains(view)) {
            views.add(view)
        }
    }

    fun removeView() {
        views.clear()
    }

    fun dispatchTouchEvent(event: MotionEvent) {
        views.forEach {
            if (it is HMGameView) {
                it.dispatchGameEvent(event)
            } else if (it is ATGameView) {
                it.dispatchGameEvent(event)
            }
        }
    }
}