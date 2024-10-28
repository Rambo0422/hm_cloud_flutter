package com.sayx.hm_cloud.callback

interface RequestDeviceSuccess {
    fun onRequestDeviceSuccess()

    fun onRequestDeviceFailed(errorMessage: String)
}