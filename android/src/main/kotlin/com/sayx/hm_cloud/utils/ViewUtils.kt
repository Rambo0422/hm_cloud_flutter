package com.sayx.hm_cloud.utils

import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt

object ViewUtils {

    fun getViewLeft(view: View): Float {
        return view.x
    }

    fun getViewTop(view: View): Float {
        return view.y
    }

    fun getViewRight(view: View): Float {
        return view.x + view.width
    }

    fun getViewBottom(view: View): Float {
        return view.y + view.height
    }

    fun getViewCenterX(view: View): Float {
        return view.x + view.width / 2f
    }

    fun getViewCenterY(view: View): Float {
        return view.y + view.height / 2f
    }

    fun getViewDistance(targetView: View, resultView: View): Float {
        val targetCenterX = targetView.x + targetView.width / 2
        val targetCenterY = targetView.y + targetView.height / 2
        val viewCenterX = resultView.x + resultView.width / 2
        val viewCenterY = resultView.y + resultView.height / 2
        return sqrt(
            (viewCenterX - targetCenterX).toDouble().pow(2.0) +
                    (viewCenterY - targetCenterY).toDouble().pow(2.0)
        ).toFloat()
    }
}