package com.example.hm_cloud.manage

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.example.hm_cloud.pluginconstant.ChannelConstant
import com.example.hm_cloud.listener.FirstFrameArrivalListener
import com.example.hm_cloud.listener.NoOperationListener
import com.haima.hmcp.Constants
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.Message
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.listeners.OnLivingListener
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import com.orhanobut.logger.Logger
import org.json.JSONException
import org.json.JSONObject

class HmcpVideoManage : HmcpPlayerListener {

    private var userId = ""
    private var pushUrl = ""
    private var isFirstFrameArrival = false

    private var firstFrameArrivalListener: FirstFrameArrivalListener? = null
    private var noOperationListener: NoOperationListener? = null

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

    fun addView(parentView: ViewGroup) {
        Logger.e("parentView addView hmcpVideoView")
        hmcpVideoView?.apply {
            if (parent == null) {
                val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                parentView.addView(hmcpVideoView, 0, layoutParams)
            }
        }

    }

    fun removeView() {
        hmcpVideoView?.apply {
            (parent as? ViewGroup)?.removeView(hmcpVideoView)
        }
    }

    fun playVideo(params: Map<String, Any>) {
        isFirstFrameArrival = false

        Logger.e("playVideo params: $params")

        val curGamePackageName = params["gameId"].toString()
        val channelId = params["channelId"].toString()

        val userId = params["userId"].toString()
        this.userId = userId

        val userToken = params["userToken"].toString()
        val isPortrait = kotlin.runCatching {
            params["isPortrait"] as Boolean
        }.getOrElse { false }

        val extraInfo = kotlin.runCatching {
            params["extraInfo"].toString()
        }.getOrElse { "" }


        val expireTime = kotlin.runCatching {
            params["expireTime"].toString().toLong()
        }.getOrElse {
            -1L
        }

        val cToken = kotlin.runCatching {
            params["token"].toString()
        }.getOrElse { "" }

        val pushUrl = kotlin.runCatching {
            params["pushUrl"].toString()
        }.getOrElse { "" }
        this.pushUrl = pushUrl

        val config = ""

        val userInfo = UserInfo()
        userInfo.userId = userId
        userInfo.userToken = userToken

        hmcpVideoView?.setUserInfo(userInfo)
        hmcpVideoView?.setConfigInfo(config)

        // 游戏包名
        val bundle = Bundle()

        bundle.putSerializable(HmcpVideoView.ORIENTATION, if (isPortrait) ScreenOrientation.PORTRAIT else ScreenOrientation.LANDSCAPE)

        bundle.putLong(HmcpVideoView.PLAY_TIME, expireTime)
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
        Logger.e("onError errorType: $errorType s: $s")
    }

    override fun onSuccess() {
        Logger.e("onSuccess: ")
    }

    override fun onExitQueue() {
    }

    override fun onMessage(message: Message) {
        Logger.e("onMessage: " + message.payload)
    }

    override fun onSceneChanged(json: String) {
        Logger.e("onSceneChanged: $json")
        try {
            val jsonObject = JSONObject(json)
            val sceneId = jsonObject.getString("sceneId")
            if (sceneId == "stop") {
                val extraInfo = jsonObject.getString("extraInfo")
                // 去除 extraInfo 头尾的冒号
                val extraInfoStr = extraInfo.substring(1, extraInfo.length - 1)
                val extraInfoJson = JSONObject("{$extraInfoStr}")
                val reason = extraInfoJson.getString("reason")
                if (reason == "no_operation"){
                    noOperationListener?.noOperation()
                    // 说明长时间未操作，被海马云系统自动销毁了
                    onDestroy()
                    // 发送消息
                    MethodChannelManage.getInstance().invokeMethod("videoFailed")
                }
            }

        } catch (e: Exception) {
            Logger.e("onSceneChanged: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onNetworkChanged(netWorkState: NetWorkState?) {}

    override fun onPlayStatus(i: Int, l: Long, s: String?) {}

    override fun HmcpPlayerStatusCallback(messageInfo: String) {
        val cloudId = HmcpManager.getInstance().cloudId
        Logger.e("HmcpPlayerStatusCallback message: $messageInfo cloudId: $cloudId")

        try {
            val obj = JSONObject(messageInfo)
            when (obj.getInt(StatusCallbackUtil.STATUS)) {
                Constants.STATUS_PLAY_INTERNAL -> {
                    Logger.e("HmcpPlayerStatusCallback STATUS_PLAY_INTERNAL")
                    hmcpVideoView?.play()

                }
                Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                    Logger.e("HmcpPlayerStatusCallback STATUS_PLAY_INTERNAL 首帧出来了")
                    if (!isFirstFrameArrival) {
                        isFirstFrameArrival = true
                        // 首帧出来再加载 View
                        firstFrameArrivalListener?.onFirstFrameArrival()
                        startLiving(userId, pushUrl)
                    }
                }
                Constants.STATUS_WAIT_CHOOSE -> {
                    Logger.e("HmcpPlayerStatusCallback: STATUS_WAIT_CHOOSE")
                    MethodChannelManage.getInstance().invokeMethod(ChannelConstant.METHOD_CLOUD_QUEUE_INFO)
                    entryQueue()
                }
                Constants.STATUS_OPERATION_HMCP_ERROR -> {
                    Logger.e("HmcpPlayerStatusCallback STATUS_OPERATION_HMCP_ERROR: ${obj.get("data")}")
                }
                Constants.STATUS_TOAST_NO_INPUT -> {

                }

                Constants.STATUS_STOP_PLAY -> {

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
        Logger.e("onDestroy")
        removeView()
        this.firstFrameArrivalListener = null
        this.noOperationListener = null
        hmcpVideoView?.onDestroy()
        hmcpVideoView = null
    }


    fun setFirstFrameArrivalListener(firstFrameArrivalListener: FirstFrameArrivalListener) {
        this.firstFrameArrivalListener = firstFrameArrivalListener
    }

    fun removeFirstFrameArrivalListener() {
        this.firstFrameArrivalListener = null
    }


    fun setNoOperationListener(noOperationListener: NoOperationListener) {
        this.noOperationListener = noOperationListener
    }

    fun removeNoOperationListener() {
        this.noOperationListener = null
    }


    private fun startLiving(livingId: String, livingUrl: String) {
        val cloudId = HmcpManager.getInstance().cloudId
        Logger.e("startLiving cloudId: $cloudId livingId: $livingId livingUrl: $livingUrl")

        if (livingId.isNotEmpty() && livingUrl.isNotEmpty()) {
            hmcpVideoView?.startLiving(livingId, livingUrl, object : OnLivingListener {
                override fun start(success: Boolean, msg: String?) {
                    Logger.e("startLiving start success:$success msg: $msg")
                    if (success) {
                        MethodChannelManage.getInstance().invokeMethod(ChannelConstant.METHOD_VIDEO_VISIBLE)
                    }
                }

                override fun stop(success: Boolean, msg: String) {
                    Logger.e("startLiving stop :$success msg: $msg")
                }
            })
        }
    }

    fun entryQueue() {
        Logger.e("entryQueue")
        hmcpVideoView?.entryQueue()
    }

    fun exitQueue() {
        Logger.e("exitQueue")
        hmcpVideoView?.exitQueue()
    }
}