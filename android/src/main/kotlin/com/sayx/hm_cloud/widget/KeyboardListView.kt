package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.adapter.KeyboardAdapter
import com.sayx.hm_cloud.callback.KeyboardClickListener
import com.sayx.hm_cloud.callback.KeyboardListCallback
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.databinding.ViewKeyboardListBinding
import com.sayx.hm_cloud.model.ControllerConfigEvent
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.MessageEvent
import org.greenrobot.eventbus.EventBus

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
                    hide()
                    EventBus.getDefault().post(MessageEvent("addKeyboard", arg = GameConstants.gamepadConfig))
                }

                override fun onEditClick(info: ControllerInfo, position: Int) {
                    hide()
                    EventBus.getDefault().post(MessageEvent("updateKeyboard", arg = info))
                }

                override fun onDeleteClick(info: ControllerInfo, position: Int) {
                    EventBus.getDefault().post(MessageEvent("deleteKeyboard", arg = info))
                }

                override fun onUseClick(info: ControllerInfo, position: Int) {
                    LogUtils.d("onUseClick:$info")
                    if (GameManager.getGameParam()?.isVip() == true || info.isOfficial == true) {
                        EventBus.getDefault().post(ControllerConfigEvent(info))
                    } else {
                        EventBus.getDefault().post(MessageEvent("showVIP"))
                    }
                }
            }
        }
    }

    private val keyboardAdapter: KeyboardAdapter by lazy {
        KeyboardAdapter().apply {
            keyboardClickListener = object : KeyboardClickListener {
                override fun onAddClick(position: Int) {
                    hide()
                    EventBus.getDefault().post(MessageEvent("addKeyboard", arg = GameConstants.keyboardConfig))
                }

                override fun onEditClick(info: ControllerInfo, position: Int) {
                    hide()
                    EventBus.getDefault().post(MessageEvent("updateKeyboard", arg = info))
                }

                override fun onDeleteClick(info: ControllerInfo, position: Int) {
                    EventBus.getDefault().post(MessageEvent("deleteKeyboard", arg = info))
                }

                override fun onUseClick(info: ControllerInfo, position: Int) {
                    if (GameManager.getGameParam()?.isVip() == true || info.isOfficial == true) {
                        EventBus.getDefault().post(ControllerConfigEvent(info))
                    } else {
                        EventBus.getDefault().post(MessageEvent("showVIP"))
                    }
                }
            }
        }
    }

    private val gamepadCallback : KeyboardListCallback by lazy {
        object : KeyboardListCallback {
            override fun onGamepadList(list: List<ControllerInfo>) {
                gamepadAdapter.itemList = list
            }

            override fun onKeyboardList(list: List<ControllerInfo>) {
                keyboardAdapter.itemList = list
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
                // 只支持键盘
                dataBinding.tvGamepadOpen.visibility = VISIBLE
                dataBinding.rvGamepad.visibility = GONE
                GameManager.getAllKeyboard(gamepadCallback)
            }
            2 -> {
                // 只支持手柄
                dataBinding.tvKeyboardOpen.visibility = VISIBLE
                dataBinding.rvKeyboard.visibility = INVISIBLE
                GameManager.getAllGamepad(gamepadCallback)
            }
            else -> {
                GameManager.getAllKeyboard(gamepadCallback)
                GameManager.getAllGamepad(gamepadCallback)
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

        fun destroy() {
            keyboardListView = null
        }

        fun updateGamePad(gamepadList: MutableList<ControllerInfo>, msg:String) {
            keyboardListView?.let {
                it.gamepadAdapter.itemList = gamepadList
                EventBus.getDefault().post(MessageEvent(msg, arg = GameConstants.gamepadConfig))
            }
        }

        fun updateKeyboard(keyboardList: MutableList<ControllerInfo>, msg:String) {
            keyboardListView?.let {
                it.keyboardAdapter.itemList = keyboardList
                EventBus.getDefault().post(MessageEvent(msg, arg = GameConstants.keyboardConfig))
            }
        }
    }
}