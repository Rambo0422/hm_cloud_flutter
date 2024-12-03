package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import com.haima.hmcp.widgets.HmcpVideoView

class HMGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : HmcpVideoView(context, attrs, defStyleAttr) {

    init {
        TouchEventDispatcher.registerView(this)
    }
}