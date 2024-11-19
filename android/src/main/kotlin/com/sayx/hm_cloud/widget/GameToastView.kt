package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.databinding.ViewGameToastBinding

class GameToastView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val dataBinding: ViewGameToastBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_game_toast, this, true)

    private var isShow = false

    init {
        initView()

        visibility = INVISIBLE
    }

    private fun initView() {
    }

    companion object {

        fun show(viewGroup: ViewGroup, title: String, subtitle: String, drawable: Int) {

        }

        fun hide() {

        }
    }
}