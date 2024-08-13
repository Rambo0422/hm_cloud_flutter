package com.sayx.hm_cloud.utils

import android.view.View
import androidx.annotation.Size
import com.blankj.utilcode.util.ScreenUtils
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
        val width = max(screenWidth, screenHeight) - navigationBarHeight
        val multiple = width / DESIGN_WIDTH
//        LogUtils.d("screenWidth=$screenWidth, screenHeight=$screenHeight, multiple=$multiple")
        return size * multiple
    }

    // size：ScreenWidth -> result:667
    fun reconvertWidthSize(size: Int): Int {
        val width = max(screenWidth, screenHeight) - navigationBarHeight
        val multiple = width / DESIGN_WIDTH
        return size / multiple
    }

    // size ：375 -> result:ScreenHeight
    fun convertHeightSize(size: Int): Int {
        val height = min(screenWidth, screenHeight)
        val multiple = height / DESIGN_HEIGHT
        return size * multiple
    }

    // size：ScreenHeight -> result:375
    fun reconvertHeightSize(size: Int): Int {
        val height = min(screenWidth, screenHeight)
        val multiple = height / DESIGN_HEIGHT
        return size / multiple
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