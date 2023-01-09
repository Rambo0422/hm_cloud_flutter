package com.example.hm_cloud

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** HmCloudPlugin */
class HmCloudPlugin : FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler, HmCloudPluginListener {

    val TAG = this.javaClass.simpleName

    private lateinit var context: Context
    private val VIEW_TYPE = "plugins.flutter.io/hm_cloud_view"
    private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
    private var lifecycle: Lifecycle? = null
    private var mHMcpVideoNativeListener: HMcpVideoNativeListener? = null

    private lateinit var methodChannel: MethodChannel
    private val HM_CLOUD_CHANNEL_NAME = "hm_cloud_controller"

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding
        this.context = flutterPluginBinding.applicationContext

        setMethodChannel(flutterPluginBinding.binaryMessenger)

        val hMcpVideoFactory = HMcpVideoFactory(
            object : LifecycleProvider {
                override fun getLifecycle(): Lifecycle? {
                    return lifecycle
                }
            },
            this
        )

        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory(VIEW_TYPE, hMcpVideoFactory)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        lifecycle = null
        mHMcpVideoNativeListener = null
        methodChannel.setMethodCallHandler(null)
    }

    private fun setMethodChannel(binaryMessenger: BinaryMessenger) {
        methodChannel = MethodChannel(binaryMessenger, HM_CLOUD_CHANNEL_NAME)
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.e(TAG, "onMethodCall: ${call.method} eventListener: $mHMcpVideoNativeListener")
        mHMcpVideoNativeListener?.onEvent(call.method)
        when (call.method) {
            "startCloudGame" -> {
            }
            "stopGame" -> {
            }
            "fullCloudGame" -> {
            }
            else -> {}
        }
    }

    override fun setHMcpVideoNativeListener(mHMcpVideoNativeListener: HMcpVideoNativeListener) {
        this.mHMcpVideoNativeListener = mHMcpVideoNativeListener
    }

    override fun setHmcpPlayerStatusCallback(json: String) {

    }

    override fun onSuccess() {
        methodChannel.invokeMethod("startSuccess", null)
    }
}
