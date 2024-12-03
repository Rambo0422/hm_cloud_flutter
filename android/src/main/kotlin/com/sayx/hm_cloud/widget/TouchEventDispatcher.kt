package com.sayx.hm_cloud.widget

import android.view.MotionEvent
import android.view.View

object TouchEventDispatcher {

    val views: MutableList<View> = mutableListOf()

    fun registerView(view: View) {
        if (!views.contains(view)) {
            views.add(view)
        }
    }

    fun dispatchTouchEvent(event: MotionEvent) {
        views.forEach {
            val ev = MotionEvent.obtain(event)
            it.dispatchTouchEvent(ev)
            ev.recycle()
        }
    }
}