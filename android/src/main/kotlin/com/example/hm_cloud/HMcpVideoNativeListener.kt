package com.example.hm_cloud

interface HMcpVideoNativeListener {
    fun onEvent(method: String)

    fun setHmCloudPluginListener(mHmCloudPluginListener: HmCloudPluginListener)
}