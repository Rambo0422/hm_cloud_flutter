package com.sayx.hm_cloud.utils

import com.blankj.utilcode.util.ScreenUtils
import kotlin.math.max
import kotlin.math.min

object AppSizeUtils {
    const val designWidth = 667
    const val designHeight = 375

    var navigationBarHeight = 0

    private val screenWidth by lazy {
        ScreenUtils.getScreenWidth()
    }
    private val screenHeight by lazy {
        ScreenUtils.getScreenHeight()
    }

    // left ：667 -> result:ScreenWidth
    fun convertLeftSize(left: Int): Float {
        val width = max(screenWidth, screenHeight) - navigationBarHeight
        val multiple = width / designWidth.toFloat()
//        LogUtils.d("screenWidth=$screenWidth, screenHeight=$screenHeight, multiple=$multiple")
        val marginLeft = left * multiple
        return marginLeft
    }

    // left：ScreenWidth -> result:667
    fun reconvertLeftSize(left: Int): Int {
        val width = max(screenWidth, screenHeight) - navigationBarHeight
        val multiple = width / designWidth.toFloat()
        return (left / multiple).toInt()
    }

    // left ：375 -> result:ScreenHeight
    fun convertTopSize(top: Int): Float {
        val height = min(screenWidth, screenHeight)
        val multiple = height / designHeight.toFloat()
        return top * multiple
    }

    // left：ScreenHeight -> result:375
    fun reconvertTopSize(top: Int): Int {
        val height = min(screenWidth, screenHeight)
        val multiple = height / designHeight.toFloat()
        return (top / multiple).toInt()
    }
}