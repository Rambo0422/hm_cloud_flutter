package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.callback.OnKeyTouchListener
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ViewCombineKeyBinding
import com.sayx.hm_cloud.utils.AppVibrateUtils
import kotlin.math.sqrt

class CombineKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewCombineKeyBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_combine_key, this, true)

    private var isDrag = false

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    var positionListener: OnPositionChangeListener? = null

    var onKeyTouchListener: OnKeyTouchListener? = null

    private var firstTouchId = 0

    fun setKeyInfo(keyInfo: KeyInfo) {
        updateText(keyInfo.text)
    }

    fun updateText(text: String?) {
        dataBinding.tvName.text = text
        LogUtils.d("updateText:$text")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
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
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    isPressed = false
                    if (controllerStatus == ControllerStatus.Edit) {
                        val location = IntArray(2)
                        getLocationOnScreen(location)
                        positionListener?.onPositionChange(location[0], location[1])
                        if (!isDrag) {
                            performClick()
                        }
                    } else if (controllerStatus == ControllerStatus.Normal) {
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
        return false
    }
}