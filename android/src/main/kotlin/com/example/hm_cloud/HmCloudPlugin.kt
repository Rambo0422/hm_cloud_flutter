package com.example.hm_cloud

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.example.hm_cloud.pluginconstant.EventConstant
import com.example.hm_cloud.manage.HmcpVideoManage
import com.example.hm_cloud.manage.MethodCallListener
import com.example.hm_cloud.manage.MethodChannelManage
import com.example.hm_cloud.pluginconstant.ChannelConstant
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
import io.flutter.plugin.common.PluginRegistry

/** HmCloudPlugin */
@Suppress("UNCHECKED_CAST")
class HmCloudPlugin : FlutterPlugin,
    ActivityAware,
    HmCloudPluginListener,
    PluginRegistry.ActivityResultListener {

    companion object {
        val TAG = "guozewen"
    }

    private lateinit var context: Context
    private var activity: Activity? = null
    private val VIEW_TYPE = "plugins.flutter.io/hm_cloud_view"
    private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
    private var lifecycle: Lifecycle? = null
    private var hmcpVideoNativeListener: HMcpVideoNativeListener? = null

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
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        lifecycle = null
        activity = null
        hmcpVideoNativeListener = null
        MethodChannelManage.getInstance().removeMethodCallHandler()
    }

    private fun setMethodChannel(binaryMessenger: BinaryMessenger) {
        MethodChannelManage.getInstance().apply {
            setMethodChannel(binaryMessenger)
            setMethodCallListener(object : MethodCallListener {
                override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
                    this@HmCloudPlugin.onMethodCall(call, result)
                }
            })
        }
    }

    fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        hmcpVideoNativeListener?.onEvent(call.method)
        when (call.method) {
            "startCloudGame" -> {
                MethodChannelManage.getInstance().invokeMethod(ChannelConstant.METHOD_CLOUD_INIT_BEGAN)
                if (activity != null) {
                    val creationParams = call.arguments as Map<String, Any>
                    hmcInit(creationParams)
                }
            }
            "stopGame" -> {
                HmcpVideoManage.getInstance().onDestroy()
            }
            "fullCloudGame" -> {
                // 跳转横屏页面
                startHmcpActivity()
            }
            else -> {}
        }
    }

    override fun setHMcpVideoNativeListener(mHMcpVideoNativeListener: HMcpVideoNativeListener) {
        this.hmcpVideoNativeListener = mHMcpVideoNativeListener
    }

    private fun hmcInit(creationParams: Map<String, Any>) {
        activity?.let { activity ->
            HmcpManagerIml.init(activity, creationParams, object : OnInitCallBackListener {
                override fun success() {
                    HmcpVideoManage.getInstance().apply {
                        initHMcpVideoView(activity)
                        playVideo(creationParams)
                        setFirstFrameArrivalListener(object : FirstFrameArrivalListener {
                            override fun onFirstFrameArrival() {
                                removeFirstFrameArrivalListener()
                                startHmcpActivity()
                            }
                        })
                    }
                }

                override fun fail(msg: String) {
                }
            })
        }
    }

    private fun startHmcpActivity() {
        // 跳转横屏移除海马云的view
        HmcpVideoManage.getInstance().removeView()

        activity?.let { activity ->
            HMcpVideoActivity.startActivityForResult(activity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == HMcpVideoActivity.REQUEST_CODE) {
            if (resultCode == HMcpVideoActivity.FULL_RESULT_CODE) {
                // 退出了全屏，加载到横屏页面
                hmcpVideoNativeListener?.onEvent(EventConstant.EVENT_EXIT_FULL)
            }
        }
        return false
    }
}
