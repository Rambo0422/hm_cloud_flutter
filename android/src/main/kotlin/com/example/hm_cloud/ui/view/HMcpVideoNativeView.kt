package com.example.hm_cloud.ui.view

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.hm_cloud.*
import com.example.hm_cloud.pluginconstant.EventConstant
import com.example.hm_cloud.manage.HmcpVideoManage
import com.example.hm_cloud.manage.MethodChannelManage
import com.example.hm_cloud.pluginconstant.ChannelConstant
import com.haima.hmcp.Constants
import com.haima.hmcp.beans.Message
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.utils.StatusCallbackUtil
import io.flutter.plugin.platform.PlatformView
import org.json.JSONObject

/**
 * 海马云原生 View
 */
class HMcpVideoNativeView(
    context: Context,
    private val lifecycleProvider: LifecycleProvider,
) : PlatformView, DefaultLifecycleObserver, HMcpVideoNativeListener {

    val TAG = "guozewen"

    private var frameLayout: InterceptTouchFrameLayout? = null
    private var disposed = false
    private var mHmCloudPluginListener: HmCloudPluginListener? = null

    init {
        lifecycleProvider.getLifecycle()?.addObserver(this)
        frameLayout = InterceptTouchFrameLayout(context)
    }

    override fun getView(): View? {
        return frameLayout
    }

    override fun dispose() {
        Log.e(TAG, "dispose")
        if (disposed) {
            return
        }
        destroyViewIfNecessary()
    }


    override fun onCreate(owner: LifecycleOwner) {
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.e(TAG, "onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.e(TAG, "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.e(TAG, "onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.e(TAG, "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.e(TAG, "onDestroy")
        if (disposed) {
            return
        }
        destroyViewIfNecessary()
    }

    private fun destroyViewIfNecessary() {
        disposed = true
        val lifecycle = lifecycleProvider.getLifecycle()
        lifecycle?.removeObserver(this)
        mHmCloudPluginListener = null
    }

    override fun onEvent(method: String) {
        Log.e(TAG, "onEvent: $method")
        when (method) {
            EventConstant.EVENT_EXIT_FULL -> {
                Log.e(TAG, "EVENT_EXIT_FULL")
                // 添加 海马云的View
                frameLayout?.apply {
                    HmcpVideoManage.getInstance().addView(this)
                }
            }
            else -> {}
        }
    }

    override fun setHmCloudPluginListener(mHmCloudPluginListener: HmCloudPluginListener) {
        this.mHmCloudPluginListener = mHmCloudPluginListener
    }
}
