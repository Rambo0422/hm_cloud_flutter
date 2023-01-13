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
import com.orhanobut.logger.Logger
import io.flutter.plugin.platform.PlatformView
import org.json.JSONObject

/**
 * 海马云原生 View
 */
class HMcpVideoNativeView(
    context: Context,
    private val lifecycleProvider: LifecycleProvider,
) : PlatformView, DefaultLifecycleObserver, HMcpVideoNativeListener {

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
        Logger.e("HMcpVideoNativeView dispose")
        if (disposed) {
            return
        }
        destroyViewIfNecessary()
    }


    override fun onCreate(owner: LifecycleOwner) {
    }

    override fun onStart(owner: LifecycleOwner) {
        Logger.e("HMcpVideoNativeView onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        Logger.e("HMcpVideoNativeView onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        Logger.e("HMcpVideoNativeView onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        Logger.e("HMcpVideoNativeView onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Logger.e("HMcpVideoNativeView onDestroy")
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
        Logger.e("HMcpVideoNativeView onEvent: $method")
        when (method) {
            EventConstant.EVENT_EXIT_FULL -> {
                Logger.e("EVENT_EXIT_FULL")
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
