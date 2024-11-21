package com.sayx.hm_cloud.callback

import com.google.gson.JsonObject
import com.sayx.hm_cloud.constants.AppVirtualOperateType

/**
 * 控制面板事件回调
 */
interface ControllerEventCallback {

    /**
     * 获取键盘数据
     */
    fun getKeyboardData()

    /**
     * 获取手柄数据
     */
    fun getGamepadData()
}