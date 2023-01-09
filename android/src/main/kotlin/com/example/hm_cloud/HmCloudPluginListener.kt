package com.example.hm_cloud

interface HmCloudPluginListener {
    fun setHMcpVideoNativeListener(mHMcpVideoNativeListener: HMcpVideoNativeListener)

    fun setHmcpPlayerStatusCallback(json: String)

    fun onSuccess()
}