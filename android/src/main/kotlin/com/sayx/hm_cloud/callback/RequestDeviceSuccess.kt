package com.sayx.hm_cloud.callback

interface RequestDeviceSuccess {

    fun onQueueTime(time:Int)

    fun onRequestDeviceSuccess()

    fun onRequestDeviceFailed(status:Int, errorMessage: String)
}