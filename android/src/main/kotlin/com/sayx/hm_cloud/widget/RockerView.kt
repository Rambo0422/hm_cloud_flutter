package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.model.CallBackMode
import com.sayx.hm_cloud.model.Direction
import com.sayx.hm_cloud.model.DirectionMode
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.AppVibrateUtils
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RockerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    // 画笔
    private val backgroundPaint: Paint
    private val rockerPaint: Paint
    private val arrowPaint: Paint

    // 中心点
    private var centerPoint: Point
    private var rockerCenterPosition: Point

    // 半径
    private var backgroundRadius = 0
    private var rockerRadius = 0
    private var arrowRadius = 0

    // 回调
    private var callBackMode = CallBackMode.CALL_BACK_MODE_MOVE
    private var angleChangeListener: OnAngleChangeListener? = null
    private var shakeListener: OnShakeListener? = null

    // 方向
    private var directionMode: DirectionMode? = null
    private var tempDirection = Direction.DIRECTION_CENTER

    // 图形
    private var backgroundDrawableMode = BACKGROUND_MODE_NULL
    private var backgroundBitmap: Bitmap? = null
    private var backgroundColor = 0
    private var backgroundSrcRect = Rect()
    private var backgroundDstRect = Rect()
    private var rockerDrawableMode = ROCKER_MODE_NULL
    private var rockerBitmap: Bitmap? = null
    private var rockerColor = 0
    private var rockerSrcRect = Rect()
    private var rockerDstRect = Rect()
    private var arrowDrawableMode = ARROW_MODE_NULL
    private var arrowBitmap: Bitmap? = null
    private var arrowSrcRect = Rect()
    private var arrowDstRect = Rect()

    private var showArrow = false

    private var arrowAngle = 270.0f

    private var isDrag = false

    private var lastX = 0f
    private var lastY = 0f

    private var parentWidth = 0
    private var parentHeight = 0

    var positionChangeListener: OnPositionChangeListener? = null
    var positionOffsetListener: OnPositionOffsetListener? = null

    private var firstTouchId = 0

    init {
        setWillNotDraw(false)

        // 获取自定义属性
        initAttribute(context, attrs)

        // 移动背景画笔
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.isDither = true

        // 摇杆画笔
        rockerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rockerPaint.isDither = true

        // 箭头画笔
        arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        arrowPaint.isDither = true

        // 中心点
        centerPoint = Point()
        rockerCenterPosition = Point()
    }

    private fun initAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerView)
        // 背景区域
        when (val background = typedArray.getDrawable(R.styleable.RockerView_rockerBackground)) {
            is BitmapDrawable -> {
                backgroundBitmap = background.bitmap
                backgroundDrawableMode = BACKGROUND_MODE_PIC
            }

            is GradientDrawable, is VectorDrawable -> {
                backgroundBitmap = drawable2Bitmap(background)
                backgroundDrawableMode = BACKGROUND_MODE_XML
            }

            is ColorDrawable -> {
                backgroundColor = background.color
                backgroundDrawableMode = BACKGROUND_MODE_COLOR
            }
        }
        // 摇杆
        when (val rocker = typedArray.getDrawable(R.styleable.RockerView_rockerScr)) {
            is BitmapDrawable -> {
                rockerBitmap = rocker.bitmap
                rockerDrawableMode = ROCKER_MODE_PIC
            }

            is GradientDrawable, is VectorDrawable -> {
                rockerBitmap = drawable2Bitmap(rocker)
                rockerDrawableMode = ROCKER_MODE_XML
            }

            is ColorDrawable -> {
                rockerColor = rocker.color
                rockerDrawableMode = ROCKER_MODE_COLOR
            }
        }
        // 箭头
        when (val arrow = typedArray.getDrawable(R.styleable.RockerView_rockerArrow)) {
            is BitmapDrawable -> {
                arrowBitmap = arrow.bitmap
                arrowDrawableMode = ARROW_MODE_PIC
            }

            is GradientDrawable, is VectorDrawable -> {
                arrowBitmap = drawable2Bitmap(arrow)
                arrowDrawableMode = ARROW_MODE_XML
            }
        }

        rockerRadius = typedArray.getDimensionPixelOffset(R.styleable.RockerView_rockerRadius, DEFAULT_ROCKER_RADIUS)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val measureWidth: Int = if (widthMode == MeasureSpec.EXACTLY) widthSize else DEFAULT_SIZE
        val measureHeight: Int = if (heightMode == MeasureSpec.EXACTLY) heightSize else DEFAULT_SIZE
        setMeasuredDimension(measureWidth, measureHeight)
    }

    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3CFFFFFF")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (controllerStatus == ControllerStatus.Edit) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }
        val cx = measuredWidth / 2
        val cy = measuredHeight / 2
        // 中心点
        centerPoint.set(cx, cy)

        arrowRadius = if (measuredWidth <= measuredHeight) cx else cy

        backgroundRadius = arrowRadius - SizeUtils.dp2px(8f)

        // 中心位置
        if (0 == rockerCenterPosition.x || 0 == rockerCenterPosition.y) {
            rockerCenterPosition.set(centerPoint.x, centerPoint.y)
        }

        // 画可移动区域
        if (BACKGROUND_MODE_PIC == backgroundDrawableMode || BACKGROUND_MODE_XML == backgroundDrawableMode) {
            // 图片
            backgroundBitmap?.let {
                backgroundSrcRect.set(0, 0, it.width, it.height)
                backgroundDstRect.set(
                    centerPoint.x - backgroundRadius,
                    centerPoint.y - backgroundRadius,
                    centerPoint.x + backgroundRadius,
                    centerPoint.y + backgroundRadius
                )
                canvas.drawBitmap(it, backgroundSrcRect, backgroundDstRect, backgroundPaint)
            }
        } else if (BACKGROUND_MODE_COLOR == backgroundDrawableMode) {
            // 色值
            backgroundPaint.color = backgroundColor
            canvas.drawCircle(centerPoint.x.toFloat(), centerPoint.y.toFloat(), backgroundRadius.toFloat(), backgroundPaint)
        }

        // 画摇杆
        if (ROCKER_MODE_PIC == rockerDrawableMode || ROCKER_MODE_XML == rockerDrawableMode) {
            rockerRadius = (arrowRadius / 2.5).toInt()
            // 图片
            rockerBitmap?.let {
                rockerSrcRect.set(0, 0, it.width, it.height)
                rockerDstRect.set(
                    rockerCenterPosition.x - rockerRadius,
                    rockerCenterPosition.y - rockerRadius,
                    rockerCenterPosition.x + rockerRadius,
                    rockerCenterPosition.y + rockerRadius
                )
                canvas.drawBitmap(it, rockerSrcRect, rockerDstRect, rockerPaint)
            }
        } else if (ROCKER_MODE_COLOR == rockerDrawableMode) {
            // 色值
            rockerPaint.color = rockerColor
            canvas.drawCircle(rockerCenterPosition.x.toFloat(), rockerCenterPosition.y.toFloat(), rockerRadius.toFloat(), rockerPaint)
        }

        // 画箭头
        if (ARROW_MODE_PIC == arrowDrawableMode || ARROW_MODE_XML == arrowDrawableMode) {
            if (showArrow) {
                arrowBitmap?.let {
                    arrowSrcRect.set(0, 0, it.width, it.height)
                    arrowDstRect.set(
                        centerPoint.x - arrowRadius,
                        centerPoint.y - arrowRadius,
                        centerPoint.x + arrowRadius,
                        centerPoint.y + arrowRadius
                    )
                    canvas.rotate(arrowAngle + 90f, centerPoint.x.toFloat(), centerPoint.y.toFloat())
                    canvas.drawBitmap(it, arrowSrcRect, arrowDstRect, arrowPaint)
                }
            }
        }
    }

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
//                        LogUtils.d("onTouchEventDOWN:${it.getPointerId(it.actionIndex)}, $firstTouchId")
                        firstTouchId = it.getPointerId(it.actionIndex)
                        AppVibrateUtils.vibrate()
                        callBackStart()
                        getRockerPositionPoint(Point(it.x.toInt(), it.y.toInt()))
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
                    } else if (controllerStatus == ControllerStatus.Normal) {
//                    LogUtils.d("onTouchEventMOVE:${it.getPointerId(it.actionIndex)}, $firstTouchId")
                        if (it.getPointerId(it.actionIndex) == firstTouchId) {
                            getRockerPositionPoint(Point(it.x.toInt(), it.y.toInt()))
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPressed = false
                    // 回调 结束
                    if (controllerStatus == ControllerStatus.Edit) {
                        val position = IntArray(4)
                        val location = AppSizeUtils.getLocationOnScreen(this, position)
                        positionChangeListener?.onPositionChange(location[0], location[1], location[2],  location[3])
                        if (parent is GameController) {
                            (parent as GameController).clearLine()
                        }
                        if (!isDrag) {
                            performClick()
                        }
                    } else if (controllerStatus == ControllerStatus.Normal) {
//                    LogUtils.d("onTouchEventUP:${it.getPointerId(it.actionIndex)}, $firstTouchId")
                        if (it.getPointerId(it.actionIndex) == firstTouchId) {
                            callBackFinish()
                            moveRocker(centerPoint)
                            showArrow = false
                            invalidate()
                        }
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    if (it.getPointerId(it.actionIndex) == firstTouchId) {
//                    LogUtils.d("onTouchEventPOINTERUP:${it.getPointerId(it.actionIndex)}, $firstTouchId")
                        callBackFinish()
                        moveRocker(centerPoint)
                        showArrow = false
                        invalidate()
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

    private fun getRockerPositionPoint(touchPoint: Point) {
        // 两点在X轴的距离
        val lenX = (touchPoint.x - centerPoint.x).toFloat()
        // 两点在Y轴距离
        val lenY = (touchPoint.y - centerPoint.y).toFloat()
        // 两点距离
        val lenXY = sqrt((lenX * lenX + lenY * lenY))
        // 计算弧度
        val radian = acos((lenX / lenXY).toDouble()) * if (touchPoint.y < centerPoint.y) -1 else 1

        val point = if (lenXY + rockerRadius <= backgroundRadius) { // 触摸位置在可活动范围内
            touchPoint
        } else { // 触摸位置在可活动范围以外
            // 计算要显示的位置
            val showPointX = (centerPoint.x + (backgroundRadius - rockerRadius) * cos(radian)).toInt()
            val showPointY = (centerPoint.y + (backgroundRadius - rockerRadius) * sin(radian)).toInt()
            Point(showPointX, showPointY)
        }

        moveRocker(point)
        showArrow = true
        invalidate()

        val distance = backgroundRadius - rockerRadius
        val offsetX = (point.x - centerPoint.x).toFloat()
        val offsetY = (centerPoint.y - point.y).toFloat()
        positionOffsetListener?.onPosition(offsetX / distance, offsetY / distance)
        // 计算角度
        arrowAngle = radian2Angle(radian)
        // 回调 返回参数
        callBack()
    }

    private fun moveRocker(point: Point) {
        rockerCenterPosition.set(point.x, point.y)
    }

    /**
     * 弧度转角度
     *
     * @param radian 弧度
     * @return 角度[0, 360)
     */
    private fun radian2Angle(radian: Double): Float {
        val result = (radian / Math.PI * 180).toFloat()
        return if (result >= 0) result else 360 + result
    }

    /**
     * Drawable 转 Bitmap
     *
     * @param drawable Drawable
     * @return Bitmap
     */
    private fun drawable2Bitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun callBackStart() {
        tempDirection = Direction.DIRECTION_CENTER
        angleChangeListener?.onStart()
        shakeListener?.onStart()
    }

    private fun callBack() {
        angleChangeListener?.angle(arrowAngle)
        shakeListener?.let {
            if (CallBackMode.CALL_BACK_MODE_MOVE == callBackMode) {
                when (directionMode) {
                    DirectionMode.DIRECTION_2_HORIZONTAL -> {
                        if (ANGLE_0 <= arrowAngle && ANGLE_HORIZONTAL_2D_OF_0P > arrowAngle || ANGLE_HORIZONTAL_2D_OF_1P <= arrowAngle && ANGLE_360 > arrowAngle) {
                            // 右
                            it.direction(Direction.DIRECTION_RIGHT)
                        } else if (ANGLE_HORIZONTAL_2D_OF_0P <= arrowAngle && ANGLE_HORIZONTAL_2D_OF_1P > arrowAngle) {
                            // 左
                            it.direction(Direction.DIRECTION_LEFT)
                        }
                    }

                    DirectionMode.DIRECTION_2_VERTICAL -> {
                        if (ANGLE_VERTICAL_2D_OF_0P <= arrowAngle && ANGLE_VERTICAL_2D_OF_1P > arrowAngle) {
                            // 下
                            it.direction(Direction.DIRECTION_DOWN)
                        } else if (ANGLE_VERTICAL_2D_OF_1P <= arrowAngle && ANGLE_360 > arrowAngle) {
                            // 上
                            it.direction(Direction.DIRECTION_UP)
                        }
                    }

                    DirectionMode.DIRECTION_4_ROTATE_0 -> {
                        if (ANGLE_4D_OF_0P <= arrowAngle && ANGLE_4D_OF_1P > arrowAngle) {
                            // 右下
                            it.direction(Direction.DIRECTION_DOWN_RIGHT)
                        } else if (ANGLE_4D_OF_1P <= arrowAngle && ANGLE_4D_OF_2P > arrowAngle) {
                            // 左下
                            it.direction(Direction.DIRECTION_DOWN_LEFT)
                        } else if (ANGLE_4D_OF_2P <= arrowAngle && ANGLE_4D_OF_3P > arrowAngle) {
                            // 左上
                            it.direction(Direction.DIRECTION_UP_LEFT)
                        } else if (ANGLE_4D_OF_3P <= arrowAngle && ANGLE_360 > arrowAngle) {
                            // 右上
                            it.direction(Direction.DIRECTION_UP_RIGHT)
                        }
                    }

                    DirectionMode.DIRECTION_4_ROTATE_45 -> {
                        if (ANGLE_0 <= arrowAngle && ANGLE_ROTATE45_4D_OF_0P > arrowAngle || ANGLE_ROTATE45_4D_OF_3P <= arrowAngle && ANGLE_360 > arrowAngle) {
                            // 右
                            it.direction(Direction.DIRECTION_RIGHT)
                        } else if (ANGLE_ROTATE45_4D_OF_0P <= arrowAngle && ANGLE_ROTATE45_4D_OF_1P > arrowAngle) {
                            // 下
                            it.direction(Direction.DIRECTION_DOWN)
                        } else if (ANGLE_ROTATE45_4D_OF_1P <= arrowAngle && ANGLE_ROTATE45_4D_OF_2P > arrowAngle) {
                            // 左
                            it.direction(Direction.DIRECTION_LEFT)
                        } else if (ANGLE_ROTATE45_4D_OF_2P <= arrowAngle && ANGLE_ROTATE45_4D_OF_3P > arrowAngle) {
                            // 上
                            it.direction(Direction.DIRECTION_UP)
                        }
                    }

                    DirectionMode.DIRECTION_8 -> {
                        if (ANGLE_0 <= arrowAngle && ANGLE_8D_OF_0P > arrowAngle || ANGLE_8D_OF_7P <= arrowAngle && ANGLE_360 > arrowAngle) {
                            // 右
                            it.direction(Direction.DIRECTION_RIGHT)
                        } else if (ANGLE_8D_OF_0P <= arrowAngle && ANGLE_8D_OF_1P > arrowAngle) {
                            // 右下
                            it.direction(Direction.DIRECTION_DOWN_RIGHT)
                        } else if (ANGLE_8D_OF_1P <= arrowAngle && ANGLE_8D_OF_2P > arrowAngle) {
                            // 下
                            it.direction(Direction.DIRECTION_DOWN)
                        } else if (ANGLE_8D_OF_2P <= arrowAngle && ANGLE_8D_OF_3P > arrowAngle) {
                            // 左下
                            it.direction(Direction.DIRECTION_DOWN_LEFT)
                        } else if (ANGLE_8D_OF_3P <= arrowAngle && ANGLE_8D_OF_4P > arrowAngle) {
                            // 左
                            it.direction(Direction.DIRECTION_LEFT)
                        } else if (ANGLE_8D_OF_4P <= arrowAngle && ANGLE_8D_OF_5P > arrowAngle) {
                            // 左上
                            it.direction(Direction.DIRECTION_UP_LEFT)
                        } else if (ANGLE_8D_OF_5P <= arrowAngle && ANGLE_8D_OF_6P > arrowAngle) {
                            // 上
                            it.direction(Direction.DIRECTION_UP)
                        } else if (ANGLE_8D_OF_6P <= arrowAngle && ANGLE_8D_OF_7P > arrowAngle) {
                            // 右上
                            it.direction(Direction.DIRECTION_UP_RIGHT)
                        }
                    }

                    else -> {}
                }
            } else if (CallBackMode.CALL_BACK_MODE_STATE_CHANGE == callBackMode) {
                when (directionMode) {
                    DirectionMode.DIRECTION_2_HORIZONTAL -> {
                        if ((ANGLE_0 <= arrowAngle && ANGLE_HORIZONTAL_2D_OF_0P > arrowAngle || ANGLE_HORIZONTAL_2D_OF_1P <= arrowAngle && ANGLE_360 > arrowAngle) && tempDirection != Direction.DIRECTION_RIGHT) {
                            // 右
                            tempDirection = Direction.DIRECTION_RIGHT
                            it.direction(Direction.DIRECTION_RIGHT)
                        } else if (ANGLE_HORIZONTAL_2D_OF_0P <= arrowAngle && ANGLE_HORIZONTAL_2D_OF_1P > arrowAngle && tempDirection != Direction.DIRECTION_LEFT) {
                            // 左
                            tempDirection = Direction.DIRECTION_LEFT
                            it.direction(Direction.DIRECTION_LEFT)
                        }
                    }

                    DirectionMode.DIRECTION_2_VERTICAL -> {
                        if (ANGLE_VERTICAL_2D_OF_0P <= arrowAngle && ANGLE_VERTICAL_2D_OF_1P > arrowAngle && tempDirection != Direction.DIRECTION_DOWN) {
                            // 下
                            tempDirection = Direction.DIRECTION_DOWN
                            it.direction(Direction.DIRECTION_DOWN)
                        } else if (ANGLE_VERTICAL_2D_OF_1P <= arrowAngle && ANGLE_360 > arrowAngle && tempDirection != Direction.DIRECTION_UP) {
                            // 上
                            tempDirection = Direction.DIRECTION_UP
                            it.direction(Direction.DIRECTION_UP)
                        }
                    }

                    DirectionMode.DIRECTION_4_ROTATE_0 -> {
                        if (ANGLE_4D_OF_0P <= arrowAngle && ANGLE_4D_OF_1P > arrowAngle && tempDirection != Direction.DIRECTION_DOWN_RIGHT) {
                            // 右下
                            tempDirection = Direction.DIRECTION_DOWN_RIGHT
                            it.direction(Direction.DIRECTION_DOWN_RIGHT)
                        } else if (ANGLE_4D_OF_1P <= arrowAngle && ANGLE_4D_OF_2P > arrowAngle && tempDirection != Direction.DIRECTION_DOWN_LEFT) {
                            // 左下
                            tempDirection = Direction.DIRECTION_DOWN_LEFT
                            it.direction(Direction.DIRECTION_DOWN_LEFT)
                        } else if (ANGLE_4D_OF_2P <= arrowAngle && ANGLE_4D_OF_3P > arrowAngle && tempDirection != Direction.DIRECTION_UP_LEFT) {
                            // 左上
                            tempDirection = Direction.DIRECTION_UP_LEFT
                            it.direction(Direction.DIRECTION_UP_LEFT)
                        } else if (ANGLE_4D_OF_3P <= arrowAngle && ANGLE_360 > arrowAngle && tempDirection != Direction.DIRECTION_UP_RIGHT) {
                            // 右上
                            tempDirection = Direction.DIRECTION_UP_RIGHT
                            it.direction(Direction.DIRECTION_UP_RIGHT)
                        }
                    }

                    DirectionMode.DIRECTION_4_ROTATE_45 -> {
                        if ((ANGLE_0 <= arrowAngle && ANGLE_ROTATE45_4D_OF_0P > arrowAngle || ANGLE_ROTATE45_4D_OF_3P <= arrowAngle && ANGLE_360 > arrowAngle) && tempDirection != Direction.DIRECTION_RIGHT) {
                            // 右
                            tempDirection = Direction.DIRECTION_RIGHT
                            it.direction(Direction.DIRECTION_RIGHT)
                        } else if (ANGLE_ROTATE45_4D_OF_0P <= arrowAngle && ANGLE_ROTATE45_4D_OF_1P > arrowAngle && tempDirection != Direction.DIRECTION_DOWN) {
                            // 下
                            tempDirection = Direction.DIRECTION_DOWN
                            it.direction(Direction.DIRECTION_DOWN)
                        } else if (ANGLE_ROTATE45_4D_OF_1P <= arrowAngle && ANGLE_ROTATE45_4D_OF_2P > arrowAngle && tempDirection != Direction.DIRECTION_LEFT) {
                            // 左
                            tempDirection = Direction.DIRECTION_LEFT
                            it.direction(Direction.DIRECTION_LEFT)
                        } else if (ANGLE_ROTATE45_4D_OF_2P <= arrowAngle && ANGLE_ROTATE45_4D_OF_3P > arrowAngle && tempDirection != Direction.DIRECTION_UP) {
                            // 上
                            tempDirection = Direction.DIRECTION_UP
                            it.direction(Direction.DIRECTION_UP)
                        }
                    }

                    DirectionMode.DIRECTION_8 -> {
                        if ((ANGLE_0 <= arrowAngle && ANGLE_8D_OF_0P > arrowAngle || ANGLE_8D_OF_7P <= arrowAngle && ANGLE_360 > arrowAngle) && tempDirection != Direction.DIRECTION_RIGHT) {
                            // 右
                            tempDirection = Direction.DIRECTION_RIGHT
                            it.direction(Direction.DIRECTION_RIGHT)
                        } else if (ANGLE_8D_OF_0P <= arrowAngle && ANGLE_8D_OF_1P > arrowAngle && tempDirection != Direction.DIRECTION_DOWN_RIGHT) {
                            // 右下
                            tempDirection = Direction.DIRECTION_DOWN_RIGHT
                            it.direction(Direction.DIRECTION_DOWN_RIGHT)
                        } else if (ANGLE_8D_OF_1P <= arrowAngle && ANGLE_8D_OF_2P > arrowAngle && tempDirection != Direction.DIRECTION_DOWN) {
                            // 下
                            tempDirection = Direction.DIRECTION_DOWN
                            it.direction(Direction.DIRECTION_DOWN)
                        } else if (ANGLE_8D_OF_2P <= arrowAngle && ANGLE_8D_OF_3P > arrowAngle && tempDirection != Direction.DIRECTION_DOWN_LEFT) {
                            // 左下
                            tempDirection = Direction.DIRECTION_DOWN_LEFT
                            it.direction(Direction.DIRECTION_DOWN_LEFT)
                        } else if (ANGLE_8D_OF_3P <= arrowAngle && ANGLE_8D_OF_4P > arrowAngle && tempDirection != Direction.DIRECTION_LEFT) {
                            // 左
                            tempDirection = Direction.DIRECTION_LEFT
                            it.direction(Direction.DIRECTION_LEFT)
                        } else if (ANGLE_8D_OF_4P <= arrowAngle && ANGLE_8D_OF_5P > arrowAngle && tempDirection != Direction.DIRECTION_UP_LEFT) {
                            // 左上
                            tempDirection = Direction.DIRECTION_UP_LEFT
                            it.direction(Direction.DIRECTION_UP_LEFT)
                        } else if (ANGLE_8D_OF_5P <= arrowAngle && ANGLE_8D_OF_6P > arrowAngle && tempDirection != Direction.DIRECTION_UP) {
                            // 上
                            tempDirection = Direction.DIRECTION_UP
                            it.direction(Direction.DIRECTION_UP)
                        } else if (ANGLE_8D_OF_6P <= arrowAngle && ANGLE_8D_OF_7P > arrowAngle && tempDirection != Direction.DIRECTION_UP_RIGHT) {
                            // 右上
                            tempDirection = Direction.DIRECTION_UP_RIGHT
                            it.direction(Direction.DIRECTION_UP_RIGHT)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun callBackFinish() {
        tempDirection = Direction.DIRECTION_CENTER
        angleChangeListener?.onFinish()
        shakeListener?.onFinish()
        positionOffsetListener?.onPosition(0.0f, 0.0f)
    }

    /**
     * 设置回调模式
     *
     * @param mode 回调模式
     */
    fun setCallBackMode(mode: CallBackMode) {
        callBackMode = mode
    }

    /**
     * 添加摇杆摇动角度的监听
     *
     * @param listener 回调接口
     */
    fun setOnAngleChangeListener(listener: OnAngleChangeListener) {
        angleChangeListener = listener
    }

    /**
     * 添加摇动的监听
     *
     * @param directionMode 监听的方向
     * @param listener      回调
     */
    fun setOnShakeListener(directionMode: DirectionMode, listener: OnShakeListener) {
        this.directionMode = directionMode
        shakeListener = listener
    }

    fun setOnPositionListener(listener: OnPositionOffsetListener) {
        this.positionOffsetListener = listener
    }

    /**
     * 摇动方向监听接口
     */
    interface OnShakeListener {
        // 开始
        fun onStart()

        /**
         * 摇动方向
         *
         * @param direction 方向
         */
        fun direction(direction: Direction?)

        // 结束
        fun onFinish()
    }

    /**
     * 摇动角度的监听接口
     */
    interface OnAngleChangeListener {
        // 开始
        fun onStart()

        /**
         * 摇杆角度变化
         *
         * @param angle 角度[0,360)
         */
        fun angle(angle: Float)

        // 结束
        fun onFinish()
    }

    interface OnPositionOffsetListener {

        fun onPosition(positionX: Float, positionY: Float)
    }

    companion object {
        private const val DEFAULT_SIZE = 400
        private const val DEFAULT_ROCKER_RADIUS = DEFAULT_SIZE / 8

        // 角度
        private const val ANGLE_0 = 0.0
        private const val ANGLE_360 = 360.0

        // 360°水平方向平分2份的边缘角度
        private const val ANGLE_HORIZONTAL_2D_OF_0P = 90.0
        private const val ANGLE_HORIZONTAL_2D_OF_1P = 270.0

        // 360°垂直方向平分2份的边缘角度
        private const val ANGLE_VERTICAL_2D_OF_0P = 0.0
        private const val ANGLE_VERTICAL_2D_OF_1P = 180.0

        // 360°平分4份的边缘角度
        private const val ANGLE_4D_OF_0P = 0.0
        private const val ANGLE_4D_OF_1P = 90.0
        private const val ANGLE_4D_OF_2P = 180.0
        private const val ANGLE_4D_OF_3P = 270.0

        // 360°平分4份的边缘角度(旋转45度)
        private const val ANGLE_ROTATE45_4D_OF_0P = 45.0
        private const val ANGLE_ROTATE45_4D_OF_1P = 135.0
        private const val ANGLE_ROTATE45_4D_OF_2P = 225.0
        private const val ANGLE_ROTATE45_4D_OF_3P = 315.0

        // 360°平分8份的边缘角度
        private const val ANGLE_8D_OF_0P = 22.5
        private const val ANGLE_8D_OF_1P = 67.5
        private const val ANGLE_8D_OF_2P = 112.5
        private const val ANGLE_8D_OF_3P = 157.5
        private const val ANGLE_8D_OF_4P = 202.5
        private const val ANGLE_8D_OF_5P = 247.5
        private const val ANGLE_8D_OF_6P = 292.5
        private const val ANGLE_8D_OF_7P = 337.5

        // 可移动区域资源类型
        private const val BACKGROUND_MODE_PIC = 0
        private const val BACKGROUND_MODE_COLOR = 1
        private const val BACKGROUND_MODE_XML = 2
        private const val BACKGROUND_MODE_NULL = 3

        // 摇杆资源类型
        private const val ROCKER_MODE_PIC = 4
        private const val ROCKER_MODE_COLOR = 5
        private const val ROCKER_MODE_XML = 6
        private const val ROCKER_MODE_NULL = 7

        // 箭头资源类型
        private const val ARROW_MODE_PIC = 8
        private const val ARROW_MODE_XML = 9
        private const val ARROW_MODE_NULL = 10
    }

    fun setBackgroundBitmap(drawable: Drawable?) {
        drawable?.let {
            this.backgroundBitmap = drawable2Bitmap(it)
            this.backgroundDrawableMode = BACKGROUND_MODE_XML
        }
    }

    fun setRockerBitmap(drawable: Drawable?) {
        drawable?.let {
            this.rockerBitmap = drawable2Bitmap(it)
            this.rockerDrawableMode = ROCKER_MODE_XML
        }
    }

    fun setArrowBitmap(drawable: Drawable?) {
        drawable?.let {
            this.arrowBitmap = drawable2Bitmap(it)
            this.arrowDrawableMode = ARROW_MODE_XML
        }
    }
}