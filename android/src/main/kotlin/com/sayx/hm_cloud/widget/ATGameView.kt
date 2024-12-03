package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import com.media.atkit.widgets.AnTongVideoView

class ATGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AnTongVideoView(context, attrs, defStyleAttr) {

    init {
        TouchEventDispatcher.registerView(this)
    }
}