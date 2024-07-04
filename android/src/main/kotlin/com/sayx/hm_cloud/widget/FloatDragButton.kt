package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sqrt

class FloatDragButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var isDrag = false

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                isDrag = false
                parent.requestDisallowInterceptTouchEvent(true)
                if (parent is ViewGroup) {
                    parentWidth = (parent as ViewGroup).width
                    parentHeight = (parent as ViewGroup).height
                }
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                isDrag = parentWidth > 0 && parentHeight > 0
                val offsetX = event.x - lastX
                val offsetY = event.y - lastY
                val distance = sqrt((offsetX * offsetX + offsetY * offsetY).toDouble())
                if (distance < 0.6) {
                    isDrag = false
                } else {
                    var moveX = x + offsetX
                    var moveY = y + offsetY
                    moveX =
                        if (moveX < 0) 0f else if (moveX > parentWidth - width) (parentWidth - width).toFloat() else moveX
                    moveY =
                        if (moveY < 0) 0f else if (moveY > parentHeight - height) (parentHeight - height).toFloat() else moveY
                    x = moveX
                    y = moveY
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isPressed = false
                if (!isDrag) {
                    performClick()
                }
            }
        }
        return true
    }
}