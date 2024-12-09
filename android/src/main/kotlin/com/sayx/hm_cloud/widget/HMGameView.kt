package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.haima.hmcp.widgets.AbsIjkVideoView
import com.haima.hmcp.widgets.HmcpVideoView

class HMGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : HmcpVideoView(context, attrs, defStyleAttr) {

    fun dispatchGameEvent(ev: MotionEvent?) {
        (hmcpVideoViewInterface as? AbsIjkVideoView)?.dispatchTouchEvent(ev)
    }

    override fun attachViewToParent(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        TouchEventDispatcher.registerView(this)
        super.attachViewToParent(child, index, params)
    }

    override fun detachViewFromParent(child: View?) {
        TouchEventDispatcher.removeView()
        super.detachViewFromParent(child)
    }
}