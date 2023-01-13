package com.example.hm_cloud.manage

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MethodChannelManage : MethodChannel.MethodCallHandler {

    private var methodCallListener: MethodCallListener? = null

    companion object {
        private const val HM_CLOUD_CHANNEL_NAME = "hm_cloud_controller"

        @Volatile
        private var instance: MethodChannelManage? = null
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: MethodChannelManage().also { instance = it }
            }
    }


    private var methodChannel: MethodChannel? = null

    fun setMethodChannel(binaryMessenger: BinaryMessenger) {
        methodChannel = MethodChannel(binaryMessenger, HM_CLOUD_CHANNEL_NAME)
        methodChannel?.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        methodCallListener?.onMethodCall(call, result)
    }

    fun invokeMethod(method: String, arguments: Any? = null) {
        methodChannel?.invokeMethod(method, arguments)
    }

    fun removeMethodCallHandler() {
        methodCallListener = null
        methodChannel?.setMethodCallHandler(null)
        methodChannel = null
    }

    fun setMethodCallListener(methodCallListener: MethodCallListener) {
        this.methodCallListener = methodCallListener
    }
}

interface MethodCallListener {
    fun onMethodCall(call: MethodCall, result: MethodChannel.Result)
}