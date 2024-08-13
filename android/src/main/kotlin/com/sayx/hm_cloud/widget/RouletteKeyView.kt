package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.callback.OnKeyEventListener
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.model.RoulettePart
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.AppVibrateUtils
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt


class RouletteKeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var radius = 0f

    private val rouletteRectF: RectF = RectF()

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var isDrag = false

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    var positionListener: OnPositionChangeListener? = null

    var onKeyEventListener: OnKeyEventListener? = null

    private var rouletteParts: MutableList<RoulettePart>? = null

    private var thumbDrawable: Drawable? = null
    private var thumbSize: Float = 0f
    private var thumbRect = Rect()
    private var thumbText: String? = ""

    var showRoulette = false

    private var firstTouchId = 0

    private var currentIndex = -1

    init {
        arcPaint.style = Paint.Style.FILL_AND_STROKE
        arcPaint.isDither = true
        arcPaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics)
        arcPaint.color = Color.parseColor("#4D000000")

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isDither = true
        textPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, context.resources.displayMetrics)

        thumbDrawable = ContextCompat.getDrawable(context, R.drawable.img_roulette_key)

        clipToOutline = false
        clipToPadding = false
        clipChildren = false

        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = width.coerceAtMost(height)
        // 整体圆角
        radius = (size - paddingLeft * 2) / 2f
        // 整体矩形
        rouletteRectF.set(paddingLeft.toFloat(), paddingLeft.toFloat(), (size - paddingLeft).toFloat(), (size - paddingLeft).toFloat())
        // thumb矩形
        thumbSize = width / 3f
        thumbRect.set(
            (rouletteRectF.left + thumbSize).toInt(),
            (rouletteRectF.top + thumbSize).toInt(),
            (rouletteRectF.right - thumbSize).toInt(),
            (rouletteRectF.bottom - thumbSize).toInt()
        )
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rouletteParts?.let {
            try {
                if (showRoulette) {
                    it.forEach { part ->
                        // 绘制圆弧
                        arcPaint.color = if (part.selected) Color.parseColor("#73C6EC4B") else Color.parseColor("#4D000000")
                        canvas.drawArc(rouletteRectF, part.startAngle, part.angle, true, arcPaint)
                        // 绘制圆弧文本
                        drawRouletteName(canvas, part)
                    }
                }
                // 绘制中心thumb
                thumbDrawable?.let { drawable ->
                    val bitmap = drawable2Bitmap(drawable, thumbSize.toInt(), thumbSize.toInt())
                    canvas.drawBitmap(bitmap, null, thumbRect, thumbPaint)
                }
                if (!TextUtils.isEmpty(thumbText)) {
                    drawCenterText(canvas, thumbText!!)
                }
            } catch (e: Exception) {
                LogUtils.e("draw fail:${e.message}")
            }
        }
    }

    private fun drawCenterText(canvas: Canvas, text: String) {
        textPaint.color = Color.parseColor("#99FFFFFF")
        val fontMetrics = textPaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = rouletteRectF.centerY() + distance
        canvas.drawText(text, rouletteRectF.centerX(), baseline, textPaint)
    }

    private fun drawRouletteName(canvas: Canvas, roulettePart: RoulettePart) {
        val name: String = roulettePart.keyInfo.text ?: ""
        val path = Path()
        path.addArc(rouletteRectF, roulettePart.startAngle, roulettePart.angle)
        val midAngle: Float = roulettePart.startAngle + roulettePart.angle / 2
        val x = rouletteRectF.centerX() + radius * cos(Math.toRadians(midAngle.toDouble())) * 0.75f
        val y = rouletteRectF.centerY() + radius * sin(Math.toRadians(midAngle.toDouble())) * 0.75f
        textPaint.color = Color.WHITE
        val fontMetrics = textPaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = y + distance
        canvas.drawText(name, x.toFloat(), baseline.toFloat(), textPaint)
    }

    fun setKeyInfo(keyInfo: KeyInfo) {
//        LogUtils.d("setKeyInfo:$keyInfo")
        if (keyInfo.composeArr.isNullOrEmpty()) {
            return
        }
        thumbText = keyInfo.text
        keyInfo.composeArr?.let {
            rouletteParts = mutableListOf()
            var startAngel = 0f
            // 360度-轮盘区域数量 * 间隔宽度
            val totalAngel = 360f
            it.forEach { info ->
                val angle = totalAngel / it.size
                val element = RoulettePart(info, startAngel, angle)
//                LogUtils.d("RoulettePart:$element")
                rouletteParts?.add(element)
                startAngel += angle
            }
        }
        showRoulette = controllerStatus != ControllerStatus.Normal
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (controllerStatus == ControllerStatus.Edit) {
                        isPressed = true
                        isDrag = false
                        if (parent is ViewGroup) {
                            parentWidth = (parent as ViewGroup).width
                            parentHeight = (parent as ViewGroup).height
                        }
                        lastX = it.x
                        lastY = it.y
                    } else if (controllerStatus == ControllerStatus.Normal) {
                        // 按压中部展示轮盘
                        val thumbTouch = isThumbTouch(event.x, event.y)
                        if (thumbTouch) {
                            firstTouchId = it.getPointerId(it.actionIndex)
                            AppVibrateUtils.vibrate()
                            showRoulette = true
                            invalidate()
                        }
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
                    } else if (controllerStatus == ControllerStatus.Normal) {
                        if (rouletteRectF.contains(event.x, event.y)) {
                            rouletteParts?.let { list ->
                                val position = computePosition(event.x, event.y)
//                                LogUtils.d("computePosition result:$position")
                                if (position != currentIndex && position >= 0 && position < list.size) {
                                    // 上一个按键抬起
                                    if (currentIndex in list.indices) {
                                        onKeyEventListener?.onButtonPress(list[currentIndex].keyInfo, false)
                                    }
                                    // 当前键按下
                                    onKeyEventListener?.onButtonPress(list[position].keyInfo, true)
                                    list.forEachIndexed { index, part ->
                                        part.selected = index == position
                                    }

                                    currentIndex = position
                                    invalidate()
                                }
                            }
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (controllerStatus == ControllerStatus.Edit) {
                        isPressed = false
                        val position = IntArray(4)
                        val location = AppSizeUtils.getLocationOnScreen(this, position)
                        positionListener?.onPositionChange(location[0], location[1], location[2],  location[3])
                        if (!isDrag) {
                            performClick()
                        }
                    } else if (controllerStatus == ControllerStatus.Normal) {
                        showRoulette = false
                        rouletteParts?.let { list ->
                            if (currentIndex in list.indices) {
                                list[currentIndex].selected = false
                                onKeyEventListener?.onButtonPress(list[currentIndex].keyInfo, false)
                            }
                        }
                        currentIndex = -1
                        invalidate()
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP -> {
                    LogUtils.d("onTouchEventPOINTER")
                }
            }
            return it.getPointerId(it.actionIndex) == firstTouchId
        }
        return false
    }

    private fun computePosition(x: Float, y: Float): Int {
        rouletteParts?.forEachIndexed { index, part ->
            if (isPointInSector(x, y, rouletteRectF.centerX(), rouletteRectF.centerY(), radius, part.startAngle, part.angle)) {
                return index
            }
        }
        return -1
    }

    private fun isPointInSector(
        pointX: Float,
        pointY: Float,
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float
    ): Boolean {

        // 计算点相对于扇形中心的角度
        var angle = atan2((pointY - centerY).toDouble(), (pointX - centerX).toDouble())
        if (angle < 0) {
            angle += 2 * Math.PI
        }
        // 转换角度至0-360度
        var angleDegrees = angle * (180.0 / Math.PI)

        // 将角度限制在0-360度范围内
        if (angleDegrees < 0) {
            angleDegrees += 360.0
        }

        // 判断点是否在扇形起始角度之后
        if (angleDegrees < startAngle) {
            angleDegrees += 360.0
        }

        // 计算扇形起始角度和终止角度
        val endAngle = startAngle + sweepAngle

        // 检查点是否在扇形的范围内
        return angleDegrees >= startAngle && angleDegrees < endAngle && hypot(
            (pointX - centerX).toDouble(),
            (pointY - centerY).toDouble()
        ) <= radius
    }

    private fun isThumbTouch(x: Float, y: Float): Boolean {
        return thumbRect.contains(x.toInt(), y.toInt())
    }

    private fun drawable2Bitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    fun updateText(text: String?) {
        thumbText = text
        invalidate()
    }
}