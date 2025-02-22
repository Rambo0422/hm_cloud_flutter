package com.sayx.hm_cloud.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.sayx.hm_cloud.utils.ViewUtils
import me.jessyan.autosize.utils.AutoSizeUtils
import kotlin.math.abs

class AlignOverlayLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var isCenterHorizontal = false
    var isCenterVertical = false

    private var selectView: View? = null
    private var matchView: View? = null

    var autoAlign = true

    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCC6EC4B")
        strokeWidth = AutoSizeUtils.dp2px(context, 1f).toFloat()
    }

//    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.RED
//        textAlign = Paint.Align.CENTER
//        textSize = AutoSizeUtils.sp2px(context, 12f).toFloat()
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        selectView?.let {
            if (isCenterHorizontal) {
                // 绘制横向居中线
                drawRelationLine(
                    canvas,
                    width / 2f,
                    0f,
                    width / 2f,
                    it.y,
                    it.y
                )
                drawRelationLine(
                    canvas,
                    width / 2f,
                    it.y + it.height,
                    width / 2f,
                    height.toFloat(),
                    height - it.y - it.height
                )
            }
            if (isCenterVertical) {
                // 绘制竖向居中
                drawRelationLine(
                    canvas,
                    0f,
                    height / 2f,
                    it.x,
                    height / 2f,
                    it.x
                )
                drawRelationLine(
                    canvas,
                    it.x + it.width,
                    height / 2f,
                    width.toFloat(),
                    height / 2f,
                    width - it.x - it.width
                )
            }
            matchView?.let { view ->
                val verticalAlign =
                    abs((view.x + view.width / 2f) - (it.x + it.width / 2f)) <= AutoSizeUtils.dp2px(context, 4f)
                if (verticalAlign) {
                    // 中心点竖向对齐
                    drawRelationLine(
                        canvas,
                        view.x + view.width / 2f,
                        0f,
                        view.x + view.width / 2f,
                        height.toFloat(),
                        0f
                    )
                }
                val horizontalAlign =
                    abs((view.y + view.height / 2f) - (it.y + it.height / 2f)) <= AutoSizeUtils.dp2px(context, 4f)
                if (horizontalAlign) {
                    // 中心点横顺向对齐
                    drawRelationLine(
                        canvas,
                        0f,
                        view.y + view.height / 2f,
                        width.toFloat(),
                        view.y + view.height / 2f,
                        0f
                    )
                }
                if (verticalAlign || horizontalAlign) {
                    return
                }
                if (abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewLeft(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 左边对齐
                    drawRelationLine(
                        canvas,
                        view.x,
                        0f,
                        view.x,
                        height.toFloat(),
                        0f
                    )
                }
                if (abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewRight(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 右侧对齐
                    drawRelationLine(
                        canvas,
                        view.x,
                        0f,
                        view.x,
                        height.toFloat(),
                        0f
                    )
                }
                if (abs(ViewUtils.getViewTop(view) - ViewUtils.getViewTop(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 上边对齐
                    drawRelationLine(
                        canvas,
                        0f,
                        view.y,
                        width.toFloat(),
                        view.y,
                        0f
                    )
                }
                if (abs(ViewUtils.getViewTop(view) - ViewUtils.getViewBottom(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 底部对齐
                    drawRelationLine(
                        canvas,
                        0f,
                        view.y,
                        width.toFloat(),
                        view.y,
                        0f
                    )
                }
                if (abs(ViewUtils.getViewRight(view) - ViewUtils.getViewRight(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 右边对齐
                    drawRelationLine(
                        canvas,
                        view.x + view.width,
                        0f,
                        view.x + view.width,
                        height.toFloat(),
                        0f
                    )
                }
                if (abs(ViewUtils.getViewRight(view) - ViewUtils.getViewLeft(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 左侧对齐
                    drawRelationLine(
                        canvas,
                        view.x + view.width,
                        0f,
                        view.x + view.width,
                        height.toFloat(),
                        0f
                    )
                }
                if (abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewBottom(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 底边对齐
                    drawRelationLine(
                        canvas,
                        0f,
                        view.y + view.height,
                        width.toFloat(),
                        view.y + view.height,
                        0f
                    )
                }
                if (abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewTop(it)) <= AutoSizeUtils.dp2px(
                        context,
                        4f
                    )
                ) {
                    // 顶部对齐
                    drawRelationLine(
                        canvas,
                        0f,
                        view.y + view.height,
                        width.toFloat(),
                        view.y + view.height,
                        0f
                    )
                }
            }
        }
    }

    private fun drawRelationLine(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        distance: Float
    ) {
        canvas.drawLine(startX, startY, endX, endY, linePaint)
//
//        val middleX = (startX + endX) / 2
//        val middleY = (startY + endY) / 2
//
//        canvas.drawText("${distance.toInt()}px", middleX, middleY, textPaint)
    }

    fun drawCenterLine(view: View) {
        selectView = view
        invalidate()
    }

    fun drawRelation(view: View, matchView: View) {
        selectView = view
        this.matchView = matchView
        invalidate()
    }

    fun clearLine() {
        if (autoAlign) {
            selectView?.let {
                if (isCenterHorizontal) {
                    it.x = width / 2f - it.width / 2f
                }
                if (isCenterVertical) {
                    it.y = height / 2f - it.height / 2f
                }
                matchView?.let { view ->
                    if (abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewLeft(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 左边对齐
                        it.x = view.x
                    }
                    if (abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewRight(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 右测对齐
                        it.x = view.x - it.width
                    }
                    if (abs(ViewUtils.getViewTop(view) - ViewUtils.getViewTop(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 上边对齐
                        it.y = view.y
                    }
                    if (abs(ViewUtils.getViewTop(view) - ViewUtils.getViewBottom(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 底部对齐
                        it.y = view.y - it.height
                    }
                    if (abs(ViewUtils.getViewRight(view) - ViewUtils.getViewRight(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 右边对齐
                        it.x = view.x + view.width - it.width
                    }
                    if (abs(ViewUtils.getViewRight(view) - ViewUtils.getViewLeft(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 左侧对齐
                        it.x = view.x + view.width
                    }
                    if (abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewBottom(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 底边对齐
                        it.y = view.y + view.height - it.height
                    }
                    if (abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewTop(it)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 顶部对齐
                        it.y = view.y + view.height
                    }
                    if (abs((view.x + view.width / 2f) - (it.x + it.width / 2f)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 中心点竖向对齐
                        it.x = view.x + view.width / 2 - it.width / 2
                    }
                    if (abs((view.y + view.height / 2f) - (it.y + it.height / 2f)) <= AutoSizeUtils.dp2px(
                            context,
                            4f
                        )
                    ) {
                        // 中心点横顺向对齐
                        it.y = view.y + view.height / 2 - it.height / 2
                    }
                }
            }
        }
        selectView = null
        matchView = null
        invalidate()
    }
}