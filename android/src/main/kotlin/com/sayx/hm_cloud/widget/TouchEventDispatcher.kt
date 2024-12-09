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
        LogUtils.d("dispatchTouchEvent:${views.size}-$event")
        views.forEach {
            LogUtils.d("dispatchTouchEvent:$it")
            if (it is HMGameView) {
                it.dispatchGameEvent(event)
            } else if (it is ATGameView) {
                it.dispatchGameEvent(event)
            }
        }
    }
}