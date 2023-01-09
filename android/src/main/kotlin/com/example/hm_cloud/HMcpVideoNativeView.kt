package com.example.hm_cloud

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.haima.hmcp.Constants
import com.haima.hmcp.beans.Message
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.listeners.OnInitCallBackListener
import com.haima.hmcp.utils.CryptoUtils
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import io.flutter.plugin.platform.PlatformView
import org.json.JSONObject

/**
 * 海马云原生 View
 */
class HMcpVideoNativeView(
    context: Context,
    private val creationParams: Map<String, Any>,
    private val lifecycleProvider: LifecycleProvider,
) : PlatformView,
    HmcpPlayerListener,
    DefaultLifecycleObserver,
    HMcpVideoNativeListener {

    val TAG = this.javaClass.simpleName

    private var frameLayout: InterceptTouchFrameLayout? = null
    private var hmcpVideoView: HmcpVideoView? = null
    private var disposed = false
    private var mHmCloudPluginListener: HmCloudPluginListener? = null

    init {
        lifecycleProvider.getLifecycle()?.addObserver(this)
        initHmcp(context, creationParams)

        frameLayout = InterceptTouchFrameLayout(context)
        hmcpVideoView = createHmcpVideoView(context)
        frameLayout?.addView(hmcpVideoView)
    }

    private fun createHmcpVideoView(context: Context): HmcpVideoView {
        val hmcpVideoView = HmcpVideoView(context)
        hmcpVideoView.hmcpPlayerListener = this@HMcpVideoNativeView
        return hmcpVideoView
    }

    override fun getView(): View? {
        Log.e(TAG, "getView hmcpVideoView: $hmcpVideoView")
        return frameLayout
    }

    override fun dispose() {
        Log.e(TAG, "dispose")
        if (disposed) {
            return
        }
        disposed = true
        destroyViewIfNecessary()
        val lifecycle = lifecycleProvider.getLifecycle()
        lifecycle?.removeObserver(this)
    }

    private fun startPlay() {
        val accessKey = creationParams["accessKey"].toString()
        val accessKeyId = creationParams["accessKeyId"].toString()
        val curGamePackageName = creationParams["gameId"].toString()
        val channelId = creationParams["channelId"].toString()
        val userId = creationParams["userId"].toString()
        val isPortrait = kotlin.runCatching {
            creationParams["isPortrait"] as Boolean
        }.getOrElse { false }

        val extraInfo = kotlin.runCatching {
            creationParams["extraInfo"].toString()
        }.getOrElse { "" }

        val playTime = kotlin.runCatching {
            creationParams["playTime"] as Int
        }.getOrElse { 1000 }

        val config = ""

        val userInfo = UserInfo()
        userInfo.userId = userId
        userInfo.userToken = userId

        hmcpVideoView?.setUserInfo(userInfo)
        hmcpVideoView?.setConfigInfo(config)

        // 游戏包名
        val bundle = Bundle()
        val cToken = CryptoUtils.generateCToken(curGamePackageName, userId, userId, accessKeyId, channelId, accessKey)

        Log.e(TAG, "isPortrait: ${isPortrait}")
        bundle.putSerializable(HmcpVideoView.ORIENTATION, if (isPortrait) ScreenOrientation.PORTRAIT else ScreenOrientation.LANDSCAPE)
        bundle.putInt(HmcpVideoView.PLAY_TIME, playTime * 1000)

        bundle.putInt(HmcpVideoView.PRIORITY, Integer.valueOf("0"))
        bundle.putString(HmcpVideoView.APP_NAME, curGamePackageName)
        bundle.putString(HmcpVideoView.APP_CHANNEL, channelId)
        bundle.putString(HmcpVideoView.C_TOKEN, cToken)

        bundle.putString(HmcpVideoView.EXTRA_ID, extraInfo)
        //STREAM_TYPE 0 rtmp, 1 webrtc, 不设置默认是rtmp
        bundle.putInt(HmcpVideoView.STREAM_TYPE, 1)
        hmcpVideoView?.play(bundle)
    }

    override fun onCreate(owner: LifecycleOwner) {
        Log.e(TAG, "onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.e(TAG, "onStart")
        hmcpVideoView?.onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        hmcpVideoView?.onResume()
        Log.e(TAG, "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.e(TAG, "onPause")
        hmcpVideoView?.onPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.e(TAG, "onStop")
        hmcpVideoView?.onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.e(TAG, "onDestroy")
        owner.lifecycle.removeObserver(this)
        // mHmCloudPluginListener = null
        if (disposed) {
            return
        }
        destroyViewIfNecessary()
    }

    private fun destroyViewIfNecessary() {
        if (hmcpVideoView == null) {
            return
        }
        hmcpVideoView?.onDestroy()
        hmcpVideoView = null
    }

    override fun onError(errorType: ErrorType, errorMEssage: String) {
        Log.e(TAG, "errorType: $errorType ---- errorMEssage: $errorMEssage")
    }

    override fun onSuccess() {
        Log.e(TAG, "onSuccess")
        mHmCloudPluginListener?.onSuccess()
    }

    override fun onExitQueue() {
        Log.e(TAG, "onExitQueue")
    }

    override fun onMessage(message: Message) {
        Log.e(TAG, "onMessage: $message")
    }

    override fun onSceneChanged(scceneChanged: String) {
        Log.e(TAG, "onSceneChanged scceneChanged: $scceneChanged")
    }

    override fun onNetworkChanged(netWorkState: NetWorkState) {
        Log.e(TAG, "onNetworkChanged netWorkState: $netWorkState")
    }

    override fun onPlayStatus(var1: Int, var2: Long, var3: String?) {
        Log.e(TAG, "onNetworkChanged var1: $var1 var2: $var2 var3: $var3")
    }

    override fun HmcpPlayerStatusCallback(json: String) {
        Log.e(TAG, "HmcpPlayerStatusCallback json: $json")
        mHmCloudPluginListener?.setHmcpPlayerStatusCallback(json)
        try {
            val jsonObject = JSONObject(json)
            val statusCode = jsonObject.getInt(StatusCallbackUtil.STATUS)
            when (statusCode) {
                Constants.STATUS_PLAY_INTERNAL -> {
                    Log.e(TAG, "HmcpPlayerStatusCallback: STATUS_PLAY_INTERNAL")
                    hmcpVideoView?.play()
                }
                Constants.STATUS_OPERATION_HMCP_ERROR -> {
                    Log.e(TAG, "HmcpPlayerStatusCallback: STATUS_OPERATION_HMCP_ERROR")
                }
                else -> {}
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPlayerError(var1: String?, var2: String?) {
        Log.e(TAG, "onPlayerError var1: $var1 var2: $var2")
    }

    override fun onInputMessage(var1: String?) {
        Log.e(TAG, "onInputMessage: ${var1}")
    }

    override fun onInputDevice(p0: Int, p1: Int) {
        Log.e(TAG, "onInputDevice")
    }

    override fun onPermissionNotGranted(p0: String?) {
        Log.e(TAG, "onPermissionNotGranted")
    }

    override fun onCloudDeviceStatus(p0: String?) {
        Log.e(TAG, "onCloudDeviceStatus")
    }

    override fun onInterceptIntent(p0: String?) {
        Log.e(TAG, "onInterceptIntent")
    }

    override fun onCloudPlayerKeyboardStatusChanged(cloudPlayerKeyboardStatus: CloudPlayerKeyboardStatus) {
        Log.e(TAG, "onCloudPlayerKeyboardStatusChanged")
    }

    private fun initHmcp(context: Context, creationParams: Map<String, Any>) {
        HmcpManagerIml.init(context, creationParams, object : OnInitCallBackListener {
            override fun success() {
//                startPlay()
            }

            override fun fail(message: String) = Unit
        })
    }

    override fun onEvent(method: String) {
        Log.e(TAG, "onEvent: $method")
        when (method) {
            "startCloudGame" -> {
                startPlay()
            }
            "stopGame" -> {
            }
            "fullCloudGame" -> {
            }
            else -> {}
        }
    }

    override fun setHmCloudPluginListener(mHmCloudPluginListener: HmCloudPluginListener) {
        this.mHmCloudPluginListener = mHmCloudPluginListener
    }
}
