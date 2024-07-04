package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.databinding.ViewEditKeyBinding
import com.sayx.hm_cloud.model.KeyInfo

class EditKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val dataBinding: ViewEditKeyBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_edit_key, this, true)

    private var data: KeyInfo? = null

    fun setData(keyInfo: KeyInfo?) {
        this.data = keyInfo
        if (keyInfo != null) {
            dataBinding.tvName.text = keyInfo.text
            dataBinding.ivDelete.visibility = VISIBLE
        } else {
            dataBinding.tvName.text = ""
            dataBinding.ivDelete.visibility = INVISIBLE
        }
    }

    fun getData(): KeyInfo? {
        return data
    }
}