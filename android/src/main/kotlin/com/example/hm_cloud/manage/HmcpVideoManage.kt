package com.example.hm_cloud.manage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.example.hmcpdemo.listener.FirstFrameArrivalListener
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
import org.json.JSONException
import org.json.JSONObject

class HmcpVideoManage : HmcpPlayerListener {

    private val TAG = HmcpVideoManage::class.java.simpleName

    companion object {
        @Volatile
        private var instance: HmcpVideoManage? = null
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: HmcpVideoManage().also { instance = it }
            }
    }

    private var hmcpVideoView: HmcpVideoView? = null

    fun initHMcpVideoView(context: Context): HmcpVideoView {
        hmcpVideoView = HmcpVideoView(context)
        hmcpVideoView?.hmcpPlayerListener = this
        return hmcpVideoView!!
    }

    fun addView(context: Context, parentView: ViewGroup) {
        Log.e(TAG, "addView")
        if (hmcpVideoView == null) {
            //  initHMcpVideoView(context)
        }

        if (hmcpVideoView?.parent == null) {
            parentView.addView(hmcpVideoView, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    fun removeView(parentView: ViewGroup) {
        if (hmcpVideoView != null && hmcpVideoView?.parent != null) {
            (hmcpVideoView?.parent as? ViewGroup)?.removeView(hmcpVideoView)
        }
    }

    fun playVideo(params: Map<String, Any>) {
        Log.i(TAG, "playVideo params: $params")

        val accessKey = params["accessKey"].toString()
        val accessKeyId = params["accessKeyId"].toString()
        val curGamePackageName = params["gameId"].toString()
        val channelId = params["channelId"].toString()
        val userId = params["userId"].toString()
        val isPortrait = kotlin.runCatching {
            params["isPortrait"] as Boolean
        }.getOrElse { false }

        val extraInfo = kotlin.runCatching {
            params["extraInfo"].toString()
        }.getOrElse { "" }

        val playTime = kotlin.runCatching {
            params["playTime"] as Int
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

        Log.e(TAG, "isPortrait: $isPortrait")
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


    override fun onError(errorType: ErrorType, s: String) {
        Log.e(TAG, "onError errorType: $errorType s: $s")
    }

    override fun onSuccess() {
        Log.e(TAG, "onSuccess: ")
    }

    override fun onExitQueue() {
    }

    override fun onMessage(message: Message) {
        Log.e(TAG, "onMessage: " + message.payload)
    }

    override fun onSceneChanged(s: String) {
        Log.e(TAG, "onSceneChanged: $s")
    }

    override fun onNetworkChanged(netWorkState: NetWorkState?) {}

    override fun onPlayStatus(i: Int, l: Long, s: String?) {}

    override fun HmcpPlayerStatusCallback(messageInfo: String) {
        Log.d(TAG, "HmcpPlayerStatusCallback message: $messageInfo")
        try {
            val obj = JSONObject(messageInfo)
            when (obj.getInt(StatusCallbackUtil.STATUS)) {
                Constants.STATUS_PLAY_INTERNAL -> {
                    hmcpVideoView?.play()
                }
                Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                    Log.d(TAG, "HmcpPlayerStatusCallback STATUS_FIRST_FRAME_ARRIVAL")
                    // 首帧出来再加载 View
                    firstFrameArrivalListener?.onFirstFrameArrival()
                }
                Constants.STATUS_OPERATION_HMCP_ERROR -> {
                    Log.d(TAG, "HmcpPlayerStatusCallback STATUS_OPERATION_HMCP_ERROR: ${obj.get("data")}")
                }
                else -> {}
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPlayerError(s: String?, s1: String?) {}

    override fun onInputMessage(s: String?) {}

    override fun onInputDevice(p0: Int, p1: Int) {

    }

    override fun onPermissionNotGranted(s: String?) {}

    override fun onCloudDeviceStatus(s: String?) {}

    override fun onInterceptIntent(s: String?) {}

    override fun onCloudPlayerKeyboardStatusChanged(cloudPlayerKeyboardStatus: CloudPlayerKeyboardStatus?) {}

    fun onDestroy() {
        Log.d(TAG, "onDestroy hmcpVideoView: $hmcpVideoView")
        this.firstFrameArrivalListener = null
        hmcpVideoView?.onDestroy()
    }

    private var firstFrameArrivalListener: FirstFrameArrivalListener? = null

    fun setFirstFrameArrivalListener(firstFrameArrivalListener: FirstFrameArrivalListener) {
        this.firstFrameArrivalListener = firstFrameArrivalListener
    }

    fun removeFirstFrameArrivalListener() {
        this.firstFrameArrivalListener = null
    }
}