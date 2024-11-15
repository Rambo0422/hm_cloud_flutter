package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.adapter.KeyboardAdapter
import com.sayx.hm_cloud.callback.KeyboardClickListener
import com.sayx.hm_cloud.databinding.ViewKeyboardListBinding
import com.sayx.hm_cloud.model.ControllerInfo

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
                override fun onAddClick(position: Int) {
                }

                override fun onEditClick(info: ControllerInfo, position: Int) {
                }

                override fun onDeleteClick(info: ControllerInfo, position: Int) {
                }

                override fun onUseClick(info: ControllerInfo, position: Int) {
                }
            }
        }
    }

    private val keyboardAdapter: KeyboardAdapter by lazy {
        KeyboardAdapter().apply {
            keyboardClickListener = object : KeyboardClickListener {
                override fun onAddClick(position: Int) {
                }

                override fun onEditClick(info: ControllerInfo, position: Int) {
                }

                override fun onDeleteClick(info: ControllerInfo, position: Int) {
                }

                override fun onUseClick(info: ControllerInfo, position: Int) {
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
        when(GameManager.getGameParam()?.supportOperation) {
            1 -> {

            }
            2 -> {

            }
            3 -> {

            }
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