package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.adapter.KeyboardAdapter
import com.sayx.hm_cloud.callback.KeyboardClickListener
import com.sayx.hm_cloud.databinding.ViewKeyboardListBinding

class KeyboardListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr)  {

    private var dataBinding: ViewKeyboardListBinding = DataBindingUtil
        .inflate(LayoutInflater.from(context), R.layout.view_keyboard_list, this, true)

    private val gamepadAdapter: KeyboardAdapter by lazy {
        KeyboardAdapter().apply {
            keyboardClickListener = object : KeyboardClickListener {
                override fun onAddClick() {

                }

                override fun onEditClick() {

                }

                override fun onDeleteClick() {

                }

                override fun onUseClick() {

                }
            }
        }
    }

    private val keyboardAdapter: KeyboardAdapter by lazy {
        KeyboardAdapter().apply {
            keyboardClickListener = object : KeyboardClickListener {
                override fun onAddClick() {

                }

                override fun onEditClick() {

                }

                override fun onDeleteClick() {

                }

                override fun onUseClick() {

                }
            }
        }
    }

    init {
        initView()
    }

    private fun initView() {
        dataBinding.ivBack.setOnClickListener {
            hide()
        }
        dataBinding.rvGamepad.layoutManager = GridLayoutManager(context, 2)
        dataBinding.rvGamepad.adapter = gamepadAdapter
        dataBinding.rvKeyboard.layoutManager = GridLayoutManager(context, 2)
        dataBinding.rvKeyboard.adapter = keyboardAdapter
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