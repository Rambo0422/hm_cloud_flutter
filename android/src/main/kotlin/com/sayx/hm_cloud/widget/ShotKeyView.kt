package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.appcompat.widget.AppCompatImageView
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.OnKeyTouchListener
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.AppVibrateUtils
import kotlin.math.sqrt

class ShotKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr)  {

    private var isDrag = false

    var needDrawShadow: Boolean = true

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    var positionListener: OnPositionChangeListener? = null

    private var firstTouchId = 0

    var onKeyTouchListener: OnKeyTouchListener? = null

    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3CFFFFFF")
        style = Paint.Style.FILL
    }

    init {
        setWillNotDraw(false)
        setImageResource(R.drawable.selector_shot_key)
    }

    fun setKeyInfo(keyInfo: KeyInfo) {
        val layoutParams = LayoutParams(
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        this.layoutParams = layoutParams
        this.alpha = keyInfo.opacity / 100f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (needDrawShadow && controllerStatus == ControllerStatus.Edit) {
            bgPaint.color = if (isActivated) Color.parseColor("#8CC6EC4B") else Color.parseColor("#3CFFFFFF")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }
    }

    private var clickTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (controllerStatus == ControllerStatus.Normal) {
                TouchEventDispatcher.dispatchTouchEvent(it)
            }
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
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
                        isPressed = true
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
            }
            return it.getPointerId(it.actionIndex) == firstTouchId
        }
        return super.dispatchTouchEvent(event)
    }
}