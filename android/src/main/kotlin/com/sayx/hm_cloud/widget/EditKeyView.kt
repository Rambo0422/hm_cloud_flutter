package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.constants.KeyType
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

    @SuppressLint("SetTextI18n")
    fun setData(keyInfo: KeyInfo?) {
        if (keyInfo != null) {
            val name: String = when(keyInfo.type) {
                KeyType.KEYBOARD_MOUSE_LEFT-> {
                    "左击"
                }
                KeyType.KEYBOARD_MOUSE_RIGHT-> {
                    "右击"
                }
                KeyType.KEYBOARD_MOUSE_MIDDLE-> {
                    "中键"
                }
                KeyType.KEYBOARD_MOUSE_UP-> {
                    "上滚"
                }
                KeyType.KEYBOARD_MOUSE_DOWN-> {
                    "下滚"
                }
                else -> {
                    keyInfo.text ?: ""
                }
            }
            dataBinding.tvName.text = name
            dataBinding.ivDelete.visibility = VISIBLE
        } else {
            dataBinding.tvName.text = ""
            dataBinding.ivDelete.visibility = INVISIBLE
        }
        this.data = keyInfo
    }

    fun getData(): KeyInfo? {
        return data
    }
}