package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.OnKeyEventListener
import com.sayx.hm_cloud.callback.OnKeyTouchListener
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ViewContainerKeyBinding
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.AppVibrateUtils
import kotlin.math.ceil
import kotlin.math.sqrt

class ContainerKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewContainerKeyBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_container_key, this, true)

    private var isDrag = false

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    var positionListener: OnPositionChangeListener? = null

    var keyEventListener: OnKeyEventListener? = null

    private var firstTouchId = 0

    var needDrawShadow = true

    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3CFFFFFF")
        style = Paint.Style.FILL
    }

    private var containerState : ContainerState = ContainerState.HIDE_LEFT

    init {
        setWillNotDraw(false)
        dataBinding.ivArrow.setOnClickListener {
            if (controllerStatus == ControllerStatus.Normal) {
                showItems()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (needDrawShadow && (controllerStatus == ControllerStatus.Edit || controllerStatus == ControllerStatus.Roulette)) {
            bgPaint.color = if (isActivated) Color.parseColor("#8CC6EC4B") else Color.parseColor("#3CFFFFFF")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }
    }

    private var clickTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (containerState == ContainerState.HIDE_LEFT || containerState == ContainerState.HIDE_RIGHT) {
                val inside = it.x >= dataBinding.ivArrow.left && it.x <= dataBinding.ivArrow.right && it.y >= dataBinding.ivArrow.top && it.y <= dataBinding.ivArrow.bottom
                if (!inside) {
                    return false
                }
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
                        firstTouchId = it.getPointerId(it.actionIndex)
                        AppVibrateUtils.vibrate()
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
                        val left = location[0]
                        val center = left + width / 2
                        if (center >= parentWidth / 2) {
                            showLeft()
                        } else {
                            showRight()
                        }
                        positionListener?.onPositionChange(left, location[1], location[2], location[3])
                        if (parent is GameController) {
                            (parent as GameController).clearLine()
                        }
                        if (!isDrag) {
                            if (System.currentTimeMillis() - clickTime > 200) {
                                clickTime = System.currentTimeMillis()
                                performClick()
                            }
                        }
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
//                    LogUtils.d("onTouchEventPOINTERDOWN:${it.getPointerId(it.actionIndex)}, $firstTouchId")
                }
            }
            return it.getPointerId(it.actionIndex) == firstTouchId
        }
        return super.onTouchEvent(event)
    }

    fun setKeyInfo(keyInfo: KeyInfo) {
        var layoutWidth = AppSizeUtils.convertViewSize(keyInfo.getKeyWidth())
        val layoutHeight = AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        val arrowLayoutParams = LayoutParams(
            layoutWidth,
            layoutHeight
        )
        dataBinding.ivArrow.layoutParams = arrowLayoutParams

        val size = keyInfo.containerArr?.size ?: 0
        val margin = AppSizeUtils.convertViewSize(ceil(6 * keyInfo.zoom / 100f * 2f).toInt())
        if (size > 0) {
            layoutWidth += layoutHeight * size + margin * (size + 1)
        }
        dataBinding.layoutItemsLeft.removeAllViews()
        dataBinding.layoutItemsRight.removeAllViews()
        keyInfo.containerArr?.forEachIndexed { _, itemInfo ->
            val itemLayoutParams = LayoutParams(
                layoutHeight,
                layoutHeight
            )
            val paddingHorizontal = margin / 2
            itemLayoutParams.marginStart = paddingHorizontal
            itemLayoutParams.marginEnd = paddingHorizontal

            val itemKeyViewLeft = ContainerItemKeyView(context).also {
                it.setKeyInfo(itemInfo, layoutHeight)
                it.onKeyTouchListener = object : OnKeyTouchListener {
                    override fun onKeyTouch(touch: Boolean) {
                        handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, 10 * 1000L)
                        if (itemInfo.click == 0) {
                            it.longClick = touch
                            keyEventListener?.onButtonPress(itemInfo, touch)
                        } else {
                            if (touch) {
                                it.longClick = !it.longClick
                                keyEventListener?.onButtonPress(itemInfo, true)
                            } else {
                                if (!it.longClick) {
                                    keyEventListener?.onButtonPress(itemInfo, false)
                                }
                            }
                        }
                    }
                }
            }

            val itemKeyViewRight = ContainerItemKeyView(context).also {
                it.setKeyInfo(itemInfo, layoutHeight)
                it.onKeyTouchListener = object : OnKeyTouchListener {
                    override fun onKeyTouch(touch: Boolean) {
                        handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, 10 * 1000L)
                        if (itemInfo.click == 0) {
                            it.longClick = touch
                            keyEventListener?.onButtonPress(itemInfo, touch)
                        } else {
                            if (touch) {
                                it.longClick = !it.longClick
                                keyEventListener?.onButtonPress(itemInfo, true)
                            } else {
                                if (!it.longClick) {
                                    keyEventListener?.onButtonPress(itemInfo, false)
                                }
                            }
                        }
                    }
                }
            }

            dataBinding.layoutItemsLeft.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
            dataBinding.layoutItemsLeft.addView(itemKeyViewLeft, itemLayoutParams)
            dataBinding.layoutItemsRight.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
            dataBinding.layoutItemsRight.addView(itemKeyViewRight, itemLayoutParams)
        }

        this.layoutParams = LayoutParams(
            layoutWidth,
            layoutHeight
        )

        dataBinding.ivArrow.setPadding(
            AppSizeUtils.convertViewSize(ceil(5 * keyInfo.zoom / 100f * 2f).toInt()),
            AppSizeUtils.convertViewSize(ceil(15 * keyInfo.zoom / 100f * 2f).toInt()),
            AppSizeUtils.convertViewSize(ceil(5 * keyInfo.zoom / 100f * 2f).toInt()),
            AppSizeUtils.convertViewSize(ceil(15 * keyInfo.zoom / 100f * 2f).toInt()),
        )
        val left = AppSizeUtils.convertViewSize(keyInfo.left)
        val center = left + layoutWidth / 2
        val screenWidth = ScreenUtils.getScreenWidth()
        if (center >= screenWidth / 2) {
            hideLeft()
        } else {
            hideRight()
        }
        invalidate()
    }

    private fun showLeft() {
        containerState = ContainerState.SHOW_LEFT
        dataBinding.ivArrow.setImageResource(R.drawable.icon_container_arrow_right)
        dataBinding.layoutItemsLeft.visibility = VISIBLE
        dataBinding.layoutItemsRight.visibility = GONE
    }

    private fun hideLeft() {
        containerState = ContainerState.HIDE_LEFT
        dataBinding.ivArrow.setImageResource(R.drawable.icon_container_arrow_left)
        dataBinding.layoutItemsLeft.visibility = INVISIBLE
        dataBinding.layoutItemsRight.visibility = GONE
    }

    private fun showRight() {
        containerState = ContainerState.SHOW_RIGHT
        dataBinding.ivArrow.setImageResource(R.drawable.icon_container_arrow_left)
        dataBinding.layoutItemsLeft.visibility = GONE
        dataBinding.layoutItemsRight.visibility = VISIBLE
    }

    private fun hideRight() {
        containerState = ContainerState.HIDE_RIGHT
        dataBinding.ivArrow.setImageResource(R.drawable.icon_container_arrow_right)
        dataBinding.layoutItemsLeft.visibility = GONE
        dataBinding.layoutItemsRight.visibility = GONE
    }

    private val runnable = Runnable {
        if (controllerStatus == ControllerStatus.Edit) {
            return@Runnable
        }
        when(containerState) {
            ContainerState.SHOW_LEFT -> {
                hideLeft()
            }
            ContainerState.SHOW_RIGHT -> {
                hideRight()
            }
            else -> {}
        }
    }

    fun showItems(keep: Boolean = false) {
        when (containerState) {
            ContainerState.HIDE_LEFT -> {
                showLeft()
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 10 * 1000L)
            }

            ContainerState.SHOW_LEFT -> {
                if (keep) {
                    showLeft()
                    handler.removeCallbacks(runnable)
                } else {
                    hideLeft()
                }
            }

            ContainerState.HIDE_RIGHT -> {
                showRight()
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 10 * 1000L)
            }

            ContainerState.SHOW_RIGHT -> {
                if (keep) {
                    showRight()
                    handler.removeCallbacks(runnable)
                } else {
                    hideRight()
                }
            }
        }
    }

    fun hideItems() {
        if (containerState == ContainerState.SHOW_LEFT) {
            hideLeft()
        } else if (containerState == ContainerState.SHOW_RIGHT) {
            hideRight()
        }
    }
}

enum class ContainerState {
    // 左侧隐藏
    HIDE_LEFT,
    // 右侧隐藏
    HIDE_RIGHT,
    // 左侧展示
    SHOW_LEFT,
    // 右侧展示
    SHOW_RIGHT
}