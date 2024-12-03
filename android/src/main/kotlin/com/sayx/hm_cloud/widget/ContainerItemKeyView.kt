package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.OnKeyTouchListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.KeyConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.constants.maps
import com.sayx.hm_cloud.databinding.ViewContainerItemKeyBinding
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.utils.AppVibrateUtils
import me.jessyan.autosize.utils.AutoSizeUtils

class ContainerItemKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dataBinding : ViewContainerItemKeyBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_container_item_key, this, true)

    var onKeyTouchListener: OnKeyTouchListener? = null

    private var firstTouchId = 0

    var longClick: Boolean = false
        set(value) {
            field = value
            isSelected = value
        }

    fun setKeyInfo(keyInfo: KeyInfo, height: Int) {
        this.layoutParams = LayoutParams(
            height,
            height
        )
        this.alpha = keyInfo.opacity / 100f
        dataBinding.tvName.layoutParams = layoutParams
        when (keyInfo.type) {
            KeyType.GAMEPAD_SQUARE, KeyType.GAMEPAD_ROUND_SMALL,
            KeyType.GAMEPAD_ROUND_MEDIUM -> {
                showKeyboardKey(keyInfo)
            }

            KeyType.KEYBOARD_KEY -> {
                if (TextUtils.isEmpty(keyInfo.map) || keyInfo.map == "map1") {
                    showKeyboardKey(keyInfo)
                } else {
                    showKeyboardMouse(keyInfo)
                }
            }

            KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT, KeyType.KEYBOARD_MOUSE_MIDDLE,
            KeyType.KEYBOARD_MOUSE_UP, KeyType.KEYBOARD_MOUSE_DOWN, KeyType.GAMEPAD_ELLIPTIC -> {
                showKeyboardMouse(keyInfo)
            }

            else -> {
                LogUtils.d("setKeyInfo:$keyInfo")
            }
        }
    }

    private fun showKeyboardKey(keyInfo: KeyInfo) {
        dataBinding.tvName.text = keyInfo.text
        val labelText = KeyConstants.keyControl[keyInfo.inputOp]
            ?: KeyConstants.keyNumber[keyInfo.inputOp]
        dataBinding.tvLabel.text = labelText
        dataBinding.tvLabel.visibility = VISIBLE
        dataBinding.ivIcon.visibility = INVISIBLE
    }

    private fun showKeyboardMouse(keyInfo: KeyInfo) {
        dataBinding.ivIcon.layoutParams = layoutParams
        dataBinding.ivIcon.visibility = VISIBLE
        when (keyInfo.type) {
            KeyType.KEYBOARD_KEY -> {
                val map = maps.find { item -> item.first == keyInfo.map}?.second
                if (map != null) {
                    // 展示label
                    val labelText = KeyConstants.keyControl[keyInfo.inputOp]
                        ?: KeyConstants.keyNumber[keyInfo.inputOp]
                    dataBinding.tvLabel.text = labelText
                    dataBinding.tvLabel.visibility = VISIBLE
                    // 缩小内容，展示边框
                    val padding = AutoSizeUtils.dp2px(context, 5f)
                    dataBinding.ivIcon.setPadding(padding, padding, padding , padding)
                    dataBinding.tvName.text = ""

                    dataBinding.ivIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            map
                        )
                    )
                } else {
                    showKeyboardKey(keyInfo)
                }
            }
            KeyType.KEYBOARD_MOUSE_LEFT -> {
                dataBinding.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selector_mouse_left
                    )
                )
            }

            KeyType.KEYBOARD_MOUSE_RIGHT -> {
                dataBinding.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selector_mouse_right
                    )
                )
            }

            KeyType.KEYBOARD_MOUSE_MIDDLE -> {
                dataBinding.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selector_mouse_middle
                    )
                )
            }

            KeyType.KEYBOARD_MOUSE_UP -> {
                dataBinding.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selector_pulley_up
                    )
                )
            }

            KeyType.KEYBOARD_MOUSE_DOWN -> {
                dataBinding.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selector_pulley_down
                    )
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (controllerStatus == ControllerStatus.Edit) {
            return false
        }
        event?.let {
//            LogUtils.d("onTouchEvent:$event")
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (controllerStatus == ControllerStatus.Normal) {
                        isPressed = true
                        firstTouchId = it.getPointerId(it.actionIndex)
                        AppVibrateUtils.vibrate()
                        onKeyTouchListener?.onKeyTouch(true)
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (controllerStatus == ControllerStatus.Normal) {
                        isPressed = false
                        if (it.getPointerId(it.actionIndex) == firstTouchId) {
                            onKeyTouchListener?.onKeyTouch(false)
                        }
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    if (it.getPointerId(it.actionIndex) == firstTouchId) {
                        onKeyTouchListener?.onKeyTouch(false)
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    LogUtils.d("onTouchEventPOINTERDOWN:${it.getPointerId(it.actionIndex)}, $firstTouchId")
                }
            }
            return it.getPointerId(it.actionIndex) == firstTouchId
        }
        return super.onTouchEvent(event)
    }
}