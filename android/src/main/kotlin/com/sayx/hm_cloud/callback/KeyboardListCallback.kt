package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.ControllerInfo

interface KeyboardListCallback {

    fun onGamepadList(list : List<ControllerInfo>)

    fun onKeyboardList(list : List<ControllerInfo>)
}