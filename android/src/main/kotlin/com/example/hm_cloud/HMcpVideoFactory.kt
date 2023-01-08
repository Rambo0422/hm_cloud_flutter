package com.example.hm_cloud

import android.content.Context
import android.util.Log
import com.haima.hmcp.utils.LogUtils
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class HMcpVideoFactory(
    private val binaryMessenger: BinaryMessenger,
    private val lifecycleProvider: LifecycleProvider
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        @Suppress("UNCHECKED_CAST")
        val creationParams = args as Map<String, Any>
        return HMcpVideoNativeView(context, viewId, binaryMessenger, creationParams, lifecycleProvider)
    }
}