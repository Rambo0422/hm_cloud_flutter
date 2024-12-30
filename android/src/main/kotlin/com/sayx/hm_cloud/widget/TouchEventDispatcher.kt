package com.sayx.hm_cloud.widget

import android.view.View

object TouchEventDispatcher {

    val views: MutableList<View> = mutableListOf()

    fun registerView(view: View) {
        removeView()
        views.add(view)
    }

    fun removeView() {
        views.clear()
    }

    fun dispatchTouchOffset(offsetX: Int, offsetY: Int) {
        views.forEach {
            if (it is ATGameView) {
                it.touchMoveOffset(offsetX, offsetY)
            }
        }
    }
}