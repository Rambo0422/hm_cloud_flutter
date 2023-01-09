package com.example.hm_cloud

import android.content.Context
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class HMcpVideoFactory(
    private val lifecycleProvider: LifecycleProvider,
    private val mHmCloudPluginListener: HmCloudPluginListener,
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        @Suppress("UNCHECKED_CAST")
        val creationParams = args as Map<String, Any>
        val hMcpVideoNativeView = HMcpVideoNativeView(context, creationParams, lifecycleProvider)
        hMcpVideoNativeView.setHmCloudPluginListener(mHmCloudPluginListener)
        mHmCloudPluginListener.setHMcpVideoNativeListener(hMcpVideoNativeView)
        return hMcpVideoNativeView
    }
}