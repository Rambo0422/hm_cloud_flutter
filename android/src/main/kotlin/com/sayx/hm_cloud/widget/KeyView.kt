package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.text.TextUtils
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
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.constants.KeyConstants
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.constants.maps
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.AppVibrateUtils
import me.jessyan.autosize.utils.AutoSizeUtils

class KeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewKeyBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_key, this, true)

    private var isDrag = false

    var needDrawShadow: Boolean = true

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
        val layoutParams = LayoutParams(
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        this.layoutParams = layoutParams
        this.alpha = keyInfo.opacity / 100f
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
        val layoutParams = LayoutParams(
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
        dataBinding.ivIcon.visibility = INVISIBLE
    }

    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3CFFFFFF")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (needDrawShadow && (controllerStatus == ControllerStatus.Edit || controllerStatus == ControllerStatus.Roulette)) {
            bgPaint.color = if (isActivated) Color.parseColor("#8CC6EC4B") else Color.parseColor("#3CFFFFFF")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }
    }

    private fun showKeyboardMouse(keyInfo: KeyInfo) {
        val layoutParams = LayoutParams(
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
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
                    dataBinding.tvName.layoutParams = layoutParams
                    dataBinding.tvName.visibility = VISIBLE

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
    }

    private var clickTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
//            LogUtils.d("onTouchEvent:$event")
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isPressed = true
                    if (controllerStatus == ControllerStatus.Edit && needDrawShadow) {
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
                    if (controllerStatus == ControllerStatus.Edit && needDrawShadow) {
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
                    if (controllerStatus == ControllerStatus.Edit && needDrawShadow) {
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