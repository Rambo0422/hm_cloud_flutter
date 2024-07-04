package com.sayx.hm_cloud.utils

import android.util.Log

object LogUtils {

    const val tag = "CloudGame"

    const val levelNone = -1
    const val levelI = 0
    const val levelV = 1
    const val levelD = 2
    const val levelW = 3
    const val levelE = 4

    var level = levelE

    fun logI(msg: String, tag: String = this.tag) {
        if (level >= levelI) {
            Log.i(tag, msg)
        }
    }

    fun logV(msg: String, tag: String = this.tag) {
        if (level >= levelV) {
            Log.v(tag, msg)
        }
    }

    fun logD(msg: String, tag: String = this.tag) {
        if (level >= levelD) {
            Log.d(tag, msg)
        }
    }

    fun logW(msg: String, tag: String = this.tag) {
        if (level >= levelW) {
            Log.w(tag, msg)
        }
    }

    fun logE(msg: String, tag: String = this.tag, throwable: Throwable? = null) {
        if (level >= levelE) {
            Log.e(tag, msg, throwable)
        }
    }
}