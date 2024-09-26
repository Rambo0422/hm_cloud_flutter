package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.callback.OnKeyTouchListener
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.databinding.ViewKeyBinding
import kotlin.math.sqrt
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.SizeUtils
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.constants.KeyConstants
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.AppVibrateUtils

class KeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewKeyBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_key, this, true)

    private var isDrag = false

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    var positionListener: OnPositionChangeListener? = null

    var onKeyTouchListener: OnKeyTouchListener? = null

    private var firstTouchId = 0

    var longClick: Boolean = false
        set(value) {
            field = value
            isSelected = value
        }

    init {
        setWillNotDraw(false)
    }

    fun setKeyInfo(keyInfo: KeyInfo) {
        when (keyInfo.type) {
            KeyType.KEYBOARD_KEY, KeyType.GAMEPAD_SQUARE, KeyType.GAMEPAD_ROUND_SMALL,
            KeyType.GAMEPAD_ROUND_MEDIUM, KeyType.KEY_COMBINE, KeyType.GAMEPAD_COMBINE -> {
                showKeyboardKey(keyInfo)
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
        val layoutParams = LayoutParams(
//            SizeUtils.dp2px(keyInfo.getKeyWidth().toFloat()),
//            SizeUtils.dp2px(keyInfo.getKeyHeight().toFloat())
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        dataBinding.tvName.text = keyInfo.text
        dataBinding.tvName.layoutParams = layoutParams
        when (keyInfo.type) {
            KeyType.KEYBOARD_KEY -> {
                val labelText = KeyConstants.keyControl[keyInfo.inputOp]
                    ?: KeyConstants.keyNumber[keyInfo.inputOp]
                dataBinding.tvLabel.text = labelText
                dataBinding.tvLabel.visibility = VISIBLE
            }

            KeyType.GAMEPAD_SQUARE -> {
                dataBinding.tvName.background =
                    ContextCompat.getDrawable(context, R.drawable.selector_key_middle_bg)
            }
        }
        dataBinding.tvName.visibility = VISIBLE
    }

    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3CFFFFFF")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (controllerStatus == ControllerStatus.Edit || controllerStatus == ControllerStatus.Roulette) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }
    }

    private fun showKeyboardMouse(keyInfo: KeyInfo) {
        val layoutParams = LayoutParams(
//            SizeUtils.dp2px(keyInfo.getKeyWidth().toFloat()),
//            SizeUtils.dp2px(keyInfo.getKeyHeight().toFloat())
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        dataBinding.ivIcon.visibility = VISIBLE
        when (keyInfo.type) {
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

            KeyType.GAMEPAD_ELLIPTIC -> {
                if (keyInfo.inputOp == GameConstants.gamepadSettingValue) {
                    dataBinding.ivIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.selector_key_setting
                        )
                    )
                }
                if (keyInfo.inputOp == GameConstants.gamepadMenuValue) {
                    dataBinding.ivIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.selector_key_menu
                        )
                    )
                }
            }
        }
        dataBinding.ivIcon.layoutParams = layoutParams
    }

    fun updateText(text: String?) {
        if (dataBinding.tvName.visibility == VISIBLE) {
            dataBinding.tvName.text = text
            LogUtils.d("updateText:$text")
        }
    }

    private var clickTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
//            LogUtils.v("onTouchEvent:$event")
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isPressed = true
                    if (controllerStatus == ControllerStatus.Edit) {
                        isDrag = false
                        if (parent is ViewGroup) {
                            parentWidth = (parent as ViewGroup).width
                            parentHeight = (parent as ViewGroup).height
                        }
                        lastX = it.x
                        lastY = it.y
                        if (parent is GameController) {
                            (parent as GameController).checkAlignment(this)
                        }
                    } else if (controllerStatus == ControllerStatus.Normal) {
                        firstTouchId = it.getPointerId(it.actionIndex)
                        AppVibrateUtils.vibrate()
                        onKeyTouchListener?.onKeyTouch(true)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (controllerStatus == ControllerStatus.Edit) {
                        isDrag = parentWidth > 0 && parentHeight > 0
                        val offsetX = it.x - lastX
                        val offsetY = it.y - lastY
                        val distance = sqrt((offsetX * offsetX + offsetY * offsetY).toDouble())
                        if (distance < 0.6) {
                            isDrag = false
                        } else {
                            var moveX = x + offsetX
                            var moveY = y + offsetY
                            val minX = (width * scaleX - width) / 2
                            val minY = (height * scaleY - height) / 2
                            val maxX = parentWidth - width - minX
                            val maxY = parentHeight - height - minY
                            moveX = if (moveX < minX) minX else if (moveX > maxX) maxX else moveX
                            moveY = if (moveY < minY) minY else if (moveY > maxY) maxY else moveY
                            x = moveX
                            y = moveY
                            if (parent is GameController) {
                                (parent as GameController).checkAlignment(this)
                            }
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    isPressed = false
                    if (controllerStatus == ControllerStatus.Edit) {
                        val position = IntArray(4)
                        val location = AppSizeUtils.getLocationOnScreen(this, position)
                        positionListener?.onPositionChange(
                            location[0],
                            location[1],
                            location[2],
                            location[3]
                        )
                        if (parent is GameController) {
                            (parent as GameController).clearLine()
                        }
                        if (!isDrag) {
                            if (System.currentTimeMillis() - clickTime > 200) {
                                clickTime = System.currentTimeMillis()
                                performClick()
                            }
                        }
                    } else if (controllerStatus == ControllerStatus.Normal) {
                        if (it.getPointerId(it.actionIndex) == firstTouchId) {
                            onKeyTouchListener?.onKeyTouch(false)
                        }
                    } else if (controllerStatus == ControllerStatus.Roulette) {
                        if (System.currentTimeMillis() - clickTime > 200) {
                            clickTime = System.currentTimeMillis()
                            performClick()
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