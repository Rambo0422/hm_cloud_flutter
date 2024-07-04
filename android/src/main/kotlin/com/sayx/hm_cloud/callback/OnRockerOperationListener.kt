package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.Direction
import com.sayx.hm_cloud.model.KeyInfo

interface OnRockerOperationListener {

    fun onRockerMove(keyInfo: KeyInfo, moveX: Float, moveY: Float)

    fun onRockerDirection(keyInfo: KeyInfo, direction: Direction?)
}