package com.sayx.hm_cloud.callback

interface RequestDeviceSuccess {
    fun onRequestDeviceSuccess()

    fun onRequestDeviceFailed(errorCode: Int, errorMessage: String)

    fun onStopPlay()
}