package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.databinding.ViewGameSettingsBinding
import com.sayx.hm_cloud.databinding.ViewKeyboardListBinding

class KeyboardListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr)  {

    private var dataBinding: ViewKeyboardListBinding = DataBindingUtil
        .inflate(LayoutInflater.from(context), R.layout.view_keyboard_list, this, true)

    init {
        initView()
    }

    private fun initView() {
        dataBinding.ivBack.setOnClickListener {
            hide()
        }
    }

    companion object {

        private var keyboardListView: KeyboardListView? = null

        fun show(viewGroup: ViewGroup) {
            if (keyboardListView != null) {
                keyboardListView?.visibility = VISIBLE
                return
            }
            keyboardListView = KeyboardListView(viewGroup.context)
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            viewGroup.addView(keyboardListView, layoutParams)
        }

        fun hide() {
            keyboardListView?.visibility = INVISIBLE
        }
    }
}