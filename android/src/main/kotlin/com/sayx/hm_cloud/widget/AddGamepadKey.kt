package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.haima.hmcp.beans.HMInputOpData
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.callback.AddKeyListener
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.databinding.ViewAddGamepadKeyBinding
import com.sayx.hm_cloud.utils.AppSizeUtils
import java.util.UUID

class AddGamepadKey @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    var showRocker: Boolean = true

    private var dataBinding: ViewAddGamepadKeyBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_add_gamepad_key, this, true)

    var isShow = false

    var listener: AddKeyListener? = null

    init {
        initView()

        visibility = INVISIBLE
    }

    private fun initView() {
        dataBinding.btnHide.setOnClickListener {
            hideBoard(null)
        }
        dataBinding.btnKeyEditLt.setOnClickListener(this)
        dataBinding.btnKeyEditLb.setOnClickListener(this)
        dataBinding.btnKeyEditLs.setOnClickListener(this)
        dataBinding.btnKeyEditRb.setOnClickListener(this)
        dataBinding.btnKeyEditRt.setOnClickListener(this)
        dataBinding.btnKeyEditRs.setOnClickListener(this)
        dataBinding.btnEditRockerL.setOnClickListener(this)
        dataBinding.btnEditRockerR.setOnClickListener(this)
        dataBinding.btnEditDPad.setOnClickListener(this)
        dataBinding.btnKeySetting.setOnClickListener(this)
        dataBinding.btnKeyMenu.setOnClickListener(this)
        dataBinding.btnKeyEditX.setOnClickListener(this)
        dataBinding.btnKeyEditY.setOnClickListener(this)
        dataBinding.btnKeyEditA.setOnClickListener(this)
        dataBinding.btnKeyEditB.setOnClickListener(this)
        dataBinding.btnHide.visibility = if (showRocker) VISIBLE else INVISIBLE
        dataBinding.btnEditRockerL.visibility = if (showRocker) VISIBLE else GONE
        dataBinding.btnEditRockerR.visibility = if (showRocker) VISIBLE else GONE
        dataBinding.btnEditDPad.visibility = if (showRocker) VISIBLE else GONE
        dataBinding.btnKeySetting.visibility = if (showRocker) VISIBLE else GONE
        dataBinding.btnKeyMenu.visibility = if (showRocker) VISIBLE else GONE
    }

    fun showBoard() {
        isShow = true
        val animator = ObjectAnimator.ofFloat(dataBinding.root, "translationY", dataBinding.root.height.toFloat(), 0.0f)
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(object : AnimatorListenerImp() {
            override fun onAnimationStart(animation: Animator) {
                visibility = VISIBLE
                dataBinding.btnEditRockerL.visibility = if (showRocker) VISIBLE else GONE
                dataBinding.btnEditRockerR.visibility = if (showRocker) VISIBLE else GONE
                dataBinding.btnEditDPad.visibility = if (showRocker) VISIBLE else GONE
                dataBinding.btnKeySetting.visibility = if (showRocker) VISIBLE else GONE
                dataBinding.btnKeyMenu.visibility = if (showRocker) VISIBLE else GONE
            }
        })
        animator.start()
    }

    fun hideBoard(listenerImp: AnimatorListenerImp?) {
        isShow = false
        val animator = ObjectAnimator.ofFloat(dataBinding.root, "translationY", 0.0f, dataBinding.root.height.toFloat())
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        listenerImp?.let {
            animator.addListener(it)
        }
        animator.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            showBoard()
        }
    }

    override fun onClick(v: View?) {
        val left = AppSizeUtils.designWidth / 2
        val top = AppSizeUtils.designHeight / 2
        var width = 0
        var zoom = 0
        var text: String? = null
        var type = ""
        val optical = 70
        var inputOp = 0
        var height = 0
        when (v?.id) {
            R.id.btn_key_edit_ls -> {
                width = 25
                height = 25
                zoom = 50
                text = "LS"
                type = KeyType.GAMEPAD_ROUND_SMALL
                inputOp = GameConstants.gamepadButtonLSValue
            }

            R.id.btn_key_edit_lb -> {
                width = 50
                height = 37
                zoom = 60
                text = "LB"
                type = KeyType.GAMEPAD_SQUARE
                inputOp = GameConstants.gamepadButtonLBValue
            }

            R.id.btn_key_edit_lt -> {
                width = 50
                height = 37
                zoom = 60
                text = "LT"
                type = KeyType.GAMEPAD_SQUARE
                inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
            }

            R.id.btn_key_edit_rs -> {
                width = 25
                height = 25
                zoom = 50
                text = "RS"
                type = KeyType.GAMEPAD_ROUND_SMALL
                inputOp = GameConstants.gamepadButtonRSValue
            }

            R.id.btn_key_edit_rb -> {
                width = 50
                height = 37
                zoom = 60
                text = "RB"
                type = KeyType.GAMEPAD_SQUARE
                inputOp = GameConstants.gamepadButtonRBValue
            }

            R.id.btn_key_edit_rt -> {
                width = 50
                height = 37
                zoom = 60
                text = "RT"
                type = KeyType.GAMEPAD_SQUARE
                inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value
            }

            R.id.btn_edit_rocker_l -> {
                width = 90
                height = 90
                zoom = 50
                type = KeyType.ROCKER_LEFT
            }

            R.id.btn_edit_rocker_r -> {
                width = 90
                height = 90
                zoom = 50
                type = KeyType.ROCKER_RIGHT
            }

            R.id.btn_key_setting -> {
                width = 40
                height = 25
                zoom = 50
                type = KeyType.GAMEPAD_ELLIPTIC
                inputOp = GameConstants.gamepadSettingValue
            }

            R.id.btn_key_menu -> {
                width = 40
                height = 25
                zoom = 50
                type = KeyType.GAMEPAD_ELLIPTIC
                inputOp = GameConstants.gamepadMenuValue
            }

            R.id.btn_edit_d_pad -> {
                width = 80
                height = 80
                zoom = 50
                type = KeyType.ROCKER_CROSS
            }

            R.id.btn_key_edit_a -> {
                width = 30
                height = 30
                zoom = 60
                text = "A"
                type = KeyType.GAMEPAD_ROUND_MEDIUM
                inputOp = GameConstants.gamepadButtonAValue
            }

            R.id.btn_key_edit_b -> {
                width = 30
                height = 30
                zoom = 60
                text = "B"
                type = KeyType.GAMEPAD_ROUND_MEDIUM
                inputOp = GameConstants.gamepadButtonBValue
            }

            R.id.btn_key_edit_x -> {
                width = 30
                height = 30
                zoom = 60
                text = "X"
                type = KeyType.GAMEPAD_ROUND_MEDIUM
                inputOp = GameConstants.gamepadButtonXValue
            }

            R.id.btn_key_edit_y -> {
                width = 30
                height = 30
                zoom = 60
                text = "Y"
                type = KeyType.GAMEPAD_ROUND_MEDIUM
                inputOp = GameConstants.gamepadButtonYValue
            }
        }
        listener?.onAddKey(KeyInfo(UUID.randomUUID(), top - height / 2, left - width / 2, width, zoom, text, type, optical, 0, inputOp, height))
    }
}