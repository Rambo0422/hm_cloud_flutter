package com.sayx.hm_cloud.utils

import android.view.View
import androidx.annotation.Size
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.LogUtils
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

object AppSizeUtils {
    const val DESIGN_WIDTH = 667
    const val DESIGN_HEIGHT = 375

    var navigationBarHeight = 0

    private val screenWidth by lazy {
        ScreenUtils.getScreenWidth()
    }
    private val screenHeight by lazy {
        ScreenUtils.getScreenHeight()
    }

    // size ：667 -> result:ScreenWidth
    fun convertWidthSize(size: Int): Int {
//        val width = max(screenWidth, screenHeight) - navigationBarHeight
        val width = max(screenWidth, screenHeight)
        val multiple: Double = width.toDouble() / DESIGN_WIDTH
//        LogUtils.d("screenWidth=$screenWidth, screenHeight=$screenHeight, widthMultiple=$multiple, navigationBarHeight:$navigationBarHeight")
        return ceil(size * multiple).toInt()
    }

    // size：ScreenWidth -> result:667
    fun reconvertWidthSize(size: Int): Int {
//        val width = max(screenWidth, screenHeight) - navigationBarHeight
        val width = max(screenWidth, screenHeight)
        val multiple: Double = width.toDouble() / DESIGN_WIDTH
//        LogUtils.d("screenWidth=$screenWidth, screenHeight=$screenHeight, widthMultiple=$multiple, navigationBarHeight:$navigationBarHeight")
        return ceil(size / multiple).toInt()
    }

    // size ：375 -> result:ScreenHeight
    fun convertHeightSize(size: Int): Int {
        val height = min(screenWidth, screenHeight)
        val multiple: Double = height.toDouble() / DESIGN_HEIGHT
//        LogUtils.d("screenWidth=$screenWidth, screenHeight=$screenHeight, heightMultiple=$multiple")
        return ceil(size * multiple).toInt()
    }

    // size：ScreenHeight -> result:375
    fun reconvertHeightSize(size: Int): Int {
        val height = min(screenWidth, screenHeight)
        val multiple: Double = height.toDouble() / DESIGN_HEIGHT
        return ceil(size / multiple).toInt()
    }

    fun convertViewSize(size: Int): Int {
        val width = max(screenWidth, screenHeight)
        val height = min(screenWidth, screenHeight)
        val widthRatio = width / DESIGN_WIDTH
        val heightRatio = height / DESIGN_HEIGHT
        return if (widthRatio > heightRatio) {
            convertHeightSize(size)
        } else {
            convertWidthSize(size)
        }
    }

    fun getLocationOnScreen(view: View, @Size(4) location: IntArray): IntArray {
        val position = IntArray(2)
        view.getLocationOnScreen(position)
        // left
        location[0] = position[0]
        // top
        location[1] = position[1]
        // right
        location[2] = screenWidth - (position[0] + view.width)
        // bottom
        location[3] = screenHeight - (position[1] + view.height)
        return location
    }
}