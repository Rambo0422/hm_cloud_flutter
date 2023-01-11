package com.example.hm_cloud

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Lifecycle
import com.example.hm_cloud.manage.HmcpVideoManage
import com.example.hm_cloud.ui.activity.HMcpVideoActivity
import com.example.hmcpdemo.listener.FirstFrameArrivalListener
import com.haima.hmcp.listeners.OnInitCallBackListener
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** HmCloudPlugin */
@Suppress("UNCHECKED_CAST")
class HmCloudPlugin : FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler, HmCloudPluginListener {

    val TAG = "HmCloudPlugin"

    private lateinit var context: Context
    private lateinit var activity: Activity

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
        activity = binding.activity
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
        mHMcpVideoNativeListener?.onEvent(call.method)
        when (call.method) {
            "startCloudGame" -> {
                if (this::activity.isInitialized) {
                    val creationParams = call.arguments as Map<String, Any>
                    hmcInit(creationParams)
                }
                // HMcpVideoActivity.start(context, call.arguments.toString())
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

    private fun hmcInit(creationParams: Map<String, Any>) {
        HmcpManagerIml.init(activity, creationParams, object : OnInitCallBackListener {
            override fun success() {
                HmcpVideoManage.getInstance().apply {
                    initHMcpVideoView(activity)
                    playVideo(creationParams)
                    setFirstFrameArrivalListener(object : FirstFrameArrivalListener {
                        override fun onFirstFrameArrival() {
                            removeFirstFrameArrivalListener()
                            HMcpVideoActivity.start(activity)
                        }
                    })
                }
            }

            override fun fail(msg: String) {
            }
        })
    }
}
