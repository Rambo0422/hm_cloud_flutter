package com.sayx.hm_cloud.utils

import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.VibrateUtils
import com.sayx.hm_cloud.constants.GameConstants

object AppVibrateUtils {

    fun vibrate(force: Boolean = false) {
        val vibrable = SPUtils.getInstance().getBoolean(GameConstants.vibrable, true)
        if (vibrable || force) {
            VibrateUtils.vibrate(50)
        }
    }
}