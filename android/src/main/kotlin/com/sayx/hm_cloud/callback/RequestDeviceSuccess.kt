package com.sayx.hm_cloud.callback

interface RequestDeviceSuccess {

    fun onQueueStatus(time:Int, rank: Int)

    fun onRequestDeviceSuccess()
}