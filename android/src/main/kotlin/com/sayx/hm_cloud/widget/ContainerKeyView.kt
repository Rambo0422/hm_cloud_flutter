package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.databinding.ViewContainerKeyBinding

class ContainerKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewContainerKeyBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_container_key, this, true)
}