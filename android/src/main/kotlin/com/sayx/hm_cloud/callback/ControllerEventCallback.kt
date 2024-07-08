package com.sayx.hm_cloud.callback

import com.google.gson.JsonObject
import com.sayx.hm_cloud.constants.AppVirtualOperateType

/**
 * 控制面板事件回调
 */
interface ControllerEventCallback {

    /**
     * 获取默认键盘数据
     */
    fun getDefaultKeyboardData()

    /**
     * 获取键盘数据
     */
    fun getKeyboardData()

    /**
     * 获取默认手柄数据
     */
    fun getDefaultGamepadData()

    /**
     * 获取手柄数据
     */
    fun getGamepadData()
    /**
     * 更新键盘数据
     */
    fun updateKeyboardData(data: JsonObject)
}