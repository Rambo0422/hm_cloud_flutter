@file:Suppress("DEPRECATION")

package com.example.hm_cloud.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.hm_cloud.R
import com.haima.hmcp.Constants
import com.haima.hmcp.beans.Message
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.utils.CryptoUtils
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import org.json.JSONObject

class HMcpVideoActivity : AppCompatActivity(), View.OnSystemUiVisibilityChangeListener, HmcpPlayerListener {

    companion object {
        private val TAG: String = HMcpVideoActivity::class.java.getSimpleName()
        private const val EXTRA_CONFIG = "config"
        fun start(context: Context, config: String) {
            Log.e(TAG, "HMcpVideoActivity start: $config")
            val intent = Intent(context, HMcpVideoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_CONFIG, config)
            context.startActivity(intent)
        }
    }

    private lateinit var hmcpVideoView: HmcpVideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.decorView.setOnSystemUiVisibilityChangeListener(this)
        setContentView(R.layout.activity_hmcp_video)

        hmcpVideoView = findViewById(R.id.hmcp_video_view)
//        hmcpVideoView.hmcpPlayerListener = this

        val creationParams = hashMapOf<String, Any>()
        creationParams["accessKey"] = "8a7a7a623d25ee7a3c87f688287bd4ba"
        creationParams["accessKeyId"] = "b14605e9d68"
        creationParams["channelId"] = "luehu"
        creationParams["userId"] = "test123"
        creationParams["gameId"] = "com.tencent.tmgp.sgame"
        creationParams["isPortrait"] = false
        creationParams["playTime"] = 1000000
        creationParams["videoViewType"] = 2

//        initHmcp(this, creationParams)
        startPlay()
    }

    private fun startPlay() {
        val extraConfig = intent.getStringExtra(EXTRA_CONFIG) ?: ""

        Log.e(TAG, "startPlay extraConfig: $extraConfig")

        val creationParams = JSONObject(extraConfig)
        Log.e(TAG, "startPlay creationParams: $extraConfig")

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

        hmcpVideoView.setUserInfo(userInfo)
        hmcpVideoView.setConfigInfo(config)

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
        hmcpVideoView.play(bundle)
    }

    @Deprecated("Deprecated in Java")
    override fun onSystemUiVisibilityChange(visibility: Int) {
        setHideVirtualKey()
    }

    private fun setHideVirtualKey() {
        //保持布局状态
        var uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or  //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN or  //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        uiOptions = if (Build.VERSION.SDK_INT >= 19) {
            uiOptions or 0x00001000
        } else {
            uiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        }
        window.decorView.systemUiVisibility = uiOptions
    }

    override fun onError(errorType: ErrorType, errorMEssage: String) {
        Log.e(TAG, "errorType: $errorType ---- errorMEssage: $errorMEssage")
    }

    override fun onSuccess() {
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
        try {
            val jsonObject = JSONObject(json)
            val statusCode = jsonObject.getInt(StatusCallbackUtil.STATUS)
            when (statusCode) {
                Constants.STATUS_PLAY_INTERNAL -> {
                    Log.e(TAG, "HmcpPlayerStatusCallback: STATUS_PLAY_INTERNAL")
                    hmcpVideoView.play()
                }
                Constants.STATUS_OPERATION_HMCP_ERROR -> {
                    Log.e(TAG, "HmcpPlayerStatusCallback: STATUS_OPERATION_HMCP_ERROR")
                }
                Constants.STATUS_PAUSE_PLAY -> {
                    // 停止游戏成功，跳转到横屏页面
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

    override fun onStart() {
        hmcpVideoView.onStart()
        super.onStart()
    }

    override fun onRestart() {
        hmcpVideoView.onRestart(1000)
        super.onRestart()
    }

    override fun onResume() {
        hmcpVideoView.onResume()
        super.onResume()
    }

    override fun onPause() {
        hmcpVideoView.onPause()
        super.onPause()
    }

    override fun onStop() {
        hmcpVideoView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        hmcpVideoView.onDestroy()
        super.onDestroy()
    }
}