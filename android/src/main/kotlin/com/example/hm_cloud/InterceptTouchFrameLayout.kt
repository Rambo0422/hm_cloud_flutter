package com.example.hm_cloud

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * 拦截触摸事件
 */
class InterceptTouchFrameLayout(context: Context) : FrameLayout(context) {

    // 屏蔽触摸事件
    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}