package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.haima.hmcp.beans.HMInputOpData
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.callback.AddKeyListener
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.constants.KeyConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.databinding.ViewAddKeyboardKeyBinding
import com.sayx.hm_cloud.utils.AppSizeUtils
import java.util.UUID

class AddKeyboardKey @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private var dataBinding: ViewAddKeyboardKeyBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_add_keyboard_key, this, true)

    var isShow = false

    var listener: AddKeyListener? = null

    var showRocker: Boolean = true

    init {

        initView()

        visibility = INVISIBLE
    }

    private fun initView() {
        configClickListener()
        dataBinding.btnSwitchKeyboard.isSelected = true
        dataBinding.btnSwitchKeyboard.elevation = 1.0f
    }

    private fun configClickListener() {
        dataBinding.btnHide.setOnClickListener {
            hideBoard(null)
        }
        dataBinding.btnSwitchKeyboard.setOnClickListener {
            val selected = dataBinding.btnSwitchKeyboard.isSelected
            if (!selected) {
                dataBinding.btnSwitchKeyboard.elevation = 1.0f
                dataBinding.btnSwitchKeyboard.isSelected = true
                dataBinding.btnSwitchPad.elevation = 0.0f
                dataBinding.btnSwitchPad.isSelected = false
                dataBinding.layoutKeyboard.visibility = VISIBLE
                dataBinding.layoutPad.visibility = GONE
            }
        }
        dataBinding.btnSwitchPad.setOnClickListener {
            val selected = dataBinding.btnSwitchPad.isSelected
            if (!selected) {
                dataBinding.btnSwitchPad.elevation = 1.0f
                dataBinding.btnSwitchPad.isSelected = true
                dataBinding.btnSwitchKeyboard.elevation = 0.0f
                dataBinding.btnSwitchKeyboard.isSelected = false
                dataBinding.layoutKeyboard.visibility = GONE
                dataBinding.layoutPad.visibility = VISIBLE
            }
        }
        dataBinding.btnEditMouseLeft.setOnClickListener(this)
        dataBinding.btnEditMouseRight.setOnClickListener(this)
        dataBinding.btnEditMouseMiddle.setOnClickListener(this)
        dataBinding.btnEditPulleyUp.setOnClickListener(this)
        dataBinding.btnEditPulleyDown.setOnClickListener(this)
        dataBinding.btnKeyEsc.setOnClickListener(this)
        dataBinding.btnKeyF1.setOnClickListener(this)
        dataBinding.btnKeyF2.setOnClickListener(this)
        dataBinding.btnKeyF3.setOnClickListener(this)
        dataBinding.btnKeyF4.setOnClickListener(this)
        dataBinding.btnKeyF5.setOnClickListener(this)
        dataBinding.btnKeyF6.setOnClickListener(this)
        dataBinding.btnKeyF7.setOnClickListener(this)
        dataBinding.btnKeyF8.setOnClickListener(this)
        dataBinding.btnKeyF9.setOnClickListener(this)
        dataBinding.btnKeyF10.setOnClickListener(this)
        dataBinding.btnKeyF11.setOnClickListener(this)
        dataBinding.btnKeyF12.setOnClickListener(this)
        dataBinding.btnKeyIns.setOnClickListener(this)
        dataBinding.btnKeyDel.setOnClickListener(this)
        dataBinding.btnKeyPgUp.setOnClickListener(this)
        dataBinding.btnKeyPgDn.setOnClickListener(this)
        dataBinding.btnKeyHome.setOnClickListener(this)
        dataBinding.btnKeyEnd.setOnClickListener(this)
        dataBinding.btnKeyBackQuote.setOnClickListener(this)
        dataBinding.btnKey1.setOnClickListener(this)
        dataBinding.btnKey1Re.setOnClickListener(this)
        dataBinding.btnKey2.setOnClickListener(this)
        dataBinding.btnKey2Re.setOnClickListener(this)
        dataBinding.btnKey3.setOnClickListener(this)
        dataBinding.btnKey3Re.setOnClickListener(this)
        dataBinding.btnKey4.setOnClickListener(this)
        dataBinding.btnKey4Re.setOnClickListener(this)
        dataBinding.btnKey5.setOnClickListener(this)
        dataBinding.btnKey5Re.setOnClickListener(this)
        dataBinding.btnKey6.setOnClickListener(this)
        dataBinding.btnKey6Re.setOnClickListener(this)
        dataBinding.btnKey7.setOnClickListener(this)
        dataBinding.btnKey7Re.setOnClickListener(this)
        dataBinding.btnKey8.setOnClickListener(this)
        dataBinding.btnKey8Re.setOnClickListener(this)
        dataBinding.btnKey9.setOnClickListener(this)
        dataBinding.btnKey9Re.setOnClickListener(this)
        dataBinding.btnKey0.setOnClickListener(this)
        dataBinding.btnKey0Re.setOnClickListener(this)
        dataBinding.btnKeyReduce.setOnClickListener(this)
        dataBinding.btnKeyReduceRe.setOnClickListener(this)
        dataBinding.btnKeyEqual.setOnClickListener(this)
        dataBinding.btnKeyBack.setOnClickListener(this)
        dataBinding.btnKeyBias.setOnClickListener(this)
        dataBinding.btnKeyBiasRe.setOnClickListener(this)
        dataBinding.btnKeyBiasReverse.setOnClickListener(this)
        dataBinding.btnKeyMul.setOnClickListener(this)
        dataBinding.btnKeyAdd.setOnClickListener(this)
        dataBinding.btnKeyTab.setOnClickListener(this)
        dataBinding.btnKeyQ.setOnClickListener(this)
        dataBinding.btnKeyW.setOnClickListener(this)
        dataBinding.btnKeyE.setOnClickListener(this)
        dataBinding.btnKeyR.setOnClickListener(this)
        dataBinding.btnKeyT.setOnClickListener(this)
        dataBinding.btnKeyY.setOnClickListener(this)
        dataBinding.btnKeyU.setOnClickListener(this)
        dataBinding.btnKeyI.setOnClickListener(this)
        dataBinding.btnKeyO.setOnClickListener(this)
        dataBinding.btnKeyP.setOnClickListener(this)
        dataBinding.btnKeyR.setOnClickListener(this)
        dataBinding.btnKeyBracketLeft.setOnClickListener(this)
        dataBinding.btnKeyBracketRight.setOnClickListener(this)
        dataBinding.btnKeyCapital.setOnClickListener(this)
        dataBinding.btnKeyA.setOnClickListener(this)
        dataBinding.btnKeyS.setOnClickListener(this)
        dataBinding.btnKeyD.setOnClickListener(this)
        dataBinding.btnKeyF.setOnClickListener(this)
        dataBinding.btnKeyG.setOnClickListener(this)
        dataBinding.btnKeyH.setOnClickListener(this)
        dataBinding.btnKeyJ.setOnClickListener(this)
        dataBinding.btnKeyK.setOnClickListener(this)
        dataBinding.btnKeyL.setOnClickListener(this)
        dataBinding.btnKeySemicolon.setOnClickListener(this)
        dataBinding.btnKeyQuote.setOnClickListener(this)
        dataBinding.btnKeyEnter.setOnClickListener(this)
        dataBinding.btnKeyEnterRe.setOnClickListener(this)
        dataBinding.btnKeyShift.setOnClickListener(this)
        dataBinding.btnKeyShiftRe.setOnClickListener(this)
        dataBinding.btnKeyZ.setOnClickListener(this)
        dataBinding.btnKeyX.setOnClickListener(this)
        dataBinding.btnKeyC.setOnClickListener(this)
        dataBinding.btnKeyV.setOnClickListener(this)
        dataBinding.btnKeyB.setOnClickListener(this)
        dataBinding.btnKeyN.setOnClickListener(this)
        dataBinding.btnKeyM.setOnClickListener(this)
        dataBinding.btnKeyComma.setOnClickListener(this)
        dataBinding.btnKeyDot.setOnClickListener(this)
        dataBinding.btnKeyDotRe.setOnClickListener(this)
        dataBinding.btnKeyUp.setOnClickListener(this)
        dataBinding.btnKeyCtrl.setOnClickListener(this)
        dataBinding.btnKeyCtrlRe.setOnClickListener(this)
        dataBinding.btnKeyAlt.setOnClickListener(this)
        dataBinding.btnKeyAltRe.setOnClickListener(this)
        dataBinding.btnKeySpace.setOnClickListener(this)
        dataBinding.btnKeyLeft.setOnClickListener(this)
        dataBinding.btnKeyDown.setOnClickListener(this)
        dataBinding.btnKeyRight.setOnClickListener(this)
        dataBinding.btnEditArrowPad.setOnClickListener(this)
        dataBinding.btnEditLetterPad.setOnClickListener(this)
    }

    fun showBoard() {
        isShow = true
        val animator = ObjectAnimator.ofFloat(dataBinding.root, "translationY", dataBinding.root.height.toFloat(), 0.0f)
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(object : AnimatorListenerImp() {
            override fun onAnimationStart(animation: Animator) {
                visibility = VISIBLE
                dataBinding.btnHide.visibility = if (showRocker) VISIBLE else INVISIBLE
                dataBinding.btnEditArrowPad.visibility = if (showRocker) VISIBLE else GONE
                dataBinding.btnEditLetterPad.visibility = if (showRocker) VISIBLE else GONE
                dataBinding.tvEditArrowPad.visibility = if (showRocker) VISIBLE else GONE
                dataBinding.tvEditLetterPad.visibility = if (showRocker) VISIBLE else GONE
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
        var width = 30
        var zoom = 60
        val text: String? = if (v is TextView) v.text?.toString() else null
        var type = ""
        var optical = 60
        var inputOp = 0
        var height = 30
        when (v?.id) {
            R.id.btn_key_0,

            R.id.btn_key_1,

            R.id.btn_key_2,

            R.id.btn_key_3,

            R.id.btn_key_4,

            R.id.btn_key_5,

            R.id.btn_key_6,

            R.id.btn_key_7,

            R.id.btn_key_8,

            R.id.btn_key_9,

            R.id.btn_key_f1,

            R.id.btn_key_f2,

            R.id.btn_key_f3,

            R.id.btn_key_f4,

            R.id.btn_key_f5,

            R.id.btn_key_f6,

            R.id.btn_key_f7,

            R.id.btn_key_f8,

            R.id.btn_key_f9,

            R.id.btn_key_f10,

            R.id.btn_key_f11,

            R.id.btn_key_f12,

            R.id.btn_key_q,

            R.id.btn_key_w,

            R.id.btn_key_e,

            R.id.btn_key_r,

            R.id.btn_key_t,

            R.id.btn_key_y,

            R.id.btn_key_u,

            R.id.btn_key_i,

            R.id.btn_key_o,

            R.id.btn_key_p,

            R.id.btn_key_a,

            R.id.btn_key_s,

            R.id.btn_key_d,

            R.id.btn_key_f,

            R.id.btn_key_g,

            R.id.btn_key_h,

            R.id.btn_key_j,

            R.id.btn_key_k,

            R.id.btn_key_l,

            R.id.btn_key_z,

            R.id.btn_key_x,

            R.id.btn_key_c,

            R.id.btn_key_v,

            R.id.btn_key_b,

            R.id.btn_key_n,

            R.id.btn_key_m,

            R.id.btn_key_esc,

            R.id.btn_key_ins,

            R.id.btn_key_del,

            R.id.btn_key_pg_up,

            R.id.btn_key_pg_dn,

            R.id.btn_key_home,

            R.id.btn_key_end,

            R.id.btn_key_tab,

            R.id.btn_key_capital,

            R.id.btn_key_shift,

            R.id.btn_key_shift_re,

            R.id.btn_key_ctrl,

            R.id.btn_key_ctrl_re,

            R.id.btn_key_alt,

            R.id.btn_key_alt_re,

            R.id.btn_key_enter,

            R.id.btn_key_back_quote,

            R.id.btn_key_reduce,

            R.id.btn_key_equal,

            R.id.btn_key_bias,

            R.id.btn_key_bracket_left,

            R.id.btn_key_bracket_right,

            R.id.btn_key_bias_reverse,

            R.id.btn_key_semicolon,

            R.id.btn_key_quote,

            R.id.btn_key_comma,

            R.id.btn_key_dot,

            R.id.btn_key_space,

            R.id.btn_key_back,

            R.id.btn_key_up,

            R.id.btn_key_left,

            R.id.btn_key_down,

            R.id.btn_key_right -> {
                inputOp = getKey(KeyConstants.keyControl, text)
                type = KeyType.KEYBOARD_KEY
            }

            R.id.btn_key_0_re,

            R.id.btn_key_1_re,

            R.id.btn_key_2_re,

            R.id.btn_key_3_re,

            R.id.btn_key_4_re,

            R.id.btn_key_5_re,

            R.id.btn_key_6_re,

            R.id.btn_key_7_re,

            R.id.btn_key_8_re,

            R.id.btn_key_9_re,

            R.id.btn_key_bias_re,

            R.id.btn_key_mul,

            R.id.btn_key_add,

            R.id.btn_key_enter_re,

            R.id.btn_key_reduce_re,

            R.id.btn_key_dot_re -> {
                inputOp = getKey(KeyConstants.keyNumber, text)
                type = KeyType.KEYBOARD_KEY
            }

            R.id.btn_edit_mouse_left -> {
                width = 40
                height = 40
                inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpMouseButtonLeft.value
                type = KeyType.KEYBOARD_MOUSE_LEFT
            }

            R.id.btn_edit_mouse_right -> {
                width = 40
                height = 40
                inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpMouseButtonRight.value
                type = KeyType.KEYBOARD_MOUSE_RIGHT
            }

            R.id.btn_edit_mouse_middle -> {
                width = 40
                height = 40
                inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpMouseButtonMiddle.value
                type = KeyType.KEYBOARD_MOUSE_MIDDLE
            }

            R.id.btn_edit_pulley_up -> {
                width = 40
                height = 40
                inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpMouseWheel.value
                type = KeyType.KEYBOARD_MOUSE_UP
            }

            R.id.btn_edit_pulley_down -> {
                width = 40
                height = 40
                inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpMouseWheel.value
                type = KeyType.KEYBOARD_MOUSE_DOWN
            }

            R.id.btn_edit_arrow_pad -> {
                width = 90
                height = 90
                optical = 80
                zoom = 70
                type = KeyType.ROCKER_ARROW
            }

            R.id.btn_edit_letter_pad -> {
                width = 90
                height = 90
                optical = 80
                zoom = 70
                type = KeyType.ROCKER_LETTER
            }
        }
        listener?.onAddKey(KeyInfo(UUID.randomUUID(), top - height / 2, left - width / 2, width, zoom, text, type, optical, 0, inputOp, height))
    }

    private fun getKey(keyMap: HashMap<Int, String>, text: String?): Int {
        var key = 0
        text?.let {
            for (entry in keyMap.entries) {
                if (entry.value == it) {
                    key = entry.key
                    return@let
                }
            }
        }
        return key
    }
}