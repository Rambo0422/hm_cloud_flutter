package com.sayx.hm_cloud

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import com.blankj.utilcode.util.ToastUtils
import com.media.atkit.AnTongManager
import com.media.atkit.Constants
import com.media.atkit.beans.UserInfo
import com.media.atkit.listeners.AnTongPlayerListener
import com.media.atkit.utils.StatusCallbackUtil
import com.media.atkit.widgets.AnTongVideoView
import com.sayx.hm_cloud.callback.RequestDeviceSuccess
import com.sayx.hm_cloud.callback.StopPlayEvent
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

object AnTongSDK {
    var anTongVideoView: AnTongVideoView? = null
    private var mRequestDeviceSuccess: RequestDeviceSuccess? = null
    private var ACCESS_KEY_ID = ""
    private const val APP_CHANNEL = "szlk"

    private val mAnTongPlayerListener = object : AnTongPlayerListener {
        override fun antongPlayerStatusCallback(callback: String?) {
            callback?.let {
                val jsonObject = JSONObject(it)
                val status = jsonObject.getInt(StatusCallbackUtil.STATUS)

                when (status) {
                    Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                        //anTongVideoView?.setHmcpPlayerListener(null)
                        // 跳转远程页面
                        mRequestDeviceSuccess?.onRequestDeviceSuccess()
                        mRequestDeviceSuccess = null

                        // 首帧出现，修改码率
                        anTongVideoView?.onSwitchResolution(20000)
                        anTongVideoView?.setVideoResolution(1920, 1080)
                        anTongVideoView?.setVideoFps(60)
                    }

//                    Constants.STATUS_PEER_REJECT,
//                    Constants.STATUS_TOKEN_INVALID,
                    Constants.STATUS_STOP_PLAY -> {
                        val stopPlayEvent = StopPlayEvent()
                        EventBus.getDefault().post(stopPlayEvent)
                    }

                    Constants.STATUS_APP_ID_ERROR,
                    Constants.STATUS_NOT_FOND_GAME,
                    Constants.STATUS_SIGN_FAILED,
                    201011,
                    Constants.STATUS_CONN_FAILED -> {
                        val errorMessage =
                            jsonObject.optString(StatusCallbackUtil.DATA, "连接失败")
                        mRequestDeviceSuccess?.onRequestDeviceFailed(errorMessage)
                    }

                    Constants.STATUS_NO_INPUT -> {
                        val errorMessage =
                            jsonObject.optString(StatusCallbackUtil.DATA, "连接失败")
                        ToastUtils.showShort(errorMessage)
                        val stopPlayEvent = StopPlayEvent()
                        EventBus.getDefault().post(stopPlayEvent)
                    }

                    Constants.STATUS_INSUFFICIENT_CLOSE -> {
                        // 余额不足
                        val errorMessage =
                            jsonObject.optString(StatusCallbackUtil.DATA, "连接失败")
                        ToastUtils.showShort(errorMessage)
                        val stopPlayEvent = StopPlayEvent()
                        EventBus.getDefault().post(stopPlayEvent)
                    }

                    else -> {}
                }
            } ?: return
        }

        override fun onPlayerError(errorCode: Int, errorMsg: String) {
            if (mRequestDeviceSuccess != null) {
                // 排队阶段
                mRequestDeviceSuccess?.onRequestDeviceFailed(errorMsg)
            } else {
                // 画面已经出现
                ToastUtils.showShort(errorMsg)
                val stopPlayEvent = StopPlayEvent()
                EventBus.getDefault().post(stopPlayEvent)
            }
        }
    }

    fun initSdk(context: Context, channelName: String, accessKeyId: String) {
        Constants.IS_DEBUG = true
        Constants.AK_DEBUG = true
        Constants.IS_TV = true
        ACCESS_KEY_ID = accessKeyId
        AnTongManager.getInstance().init(context, channelName, accessKeyId)
    }

    fun play(
        context: Context,
        userId: String,
        userToken: String,
        gameId: String,
        anTongPackageName: String,
        sign: String,
        requestDeviceSuccess: RequestDeviceSuccess
    ) {
        this.mRequestDeviceSuccess = requestDeviceSuccess

        if (anTongVideoView == null) {
            anTongVideoView = AnTongVideoView(context)
        }
        anTongVideoView?.setHmcpPlayerListener(mAnTongPlayerListener)

        val flag = 48
        val userInfo = UserInfo()
        userInfo.userId = userId
        userInfo.userToken = userToken
        userInfo.flag = flag
        anTongVideoView?.setUserInfo(userInfo)

        val bundle = Bundle()
        bundle.putInt(AnTongVideoView.PLAY_TIME, 99999)
        bundle.putInt(AnTongVideoView.VIEW_RESOLUTION_WIDTH, 1920)
        bundle.putInt(AnTongVideoView.VIEW_RESOLUTION_HEIGHT, 1080)
        bundle.putBoolean(AnTongVideoView.IS_ARCHIVE, false)

        val protoData = getProtoData(userId, gameId)
        bundle.putString(AnTongVideoView.PROTO_DATA, protoData)
        bundle.putBoolean(AnTongVideoView.AUTO_PLAY_AUDIO, true)
        bundle.putInt(AnTongVideoView.RESOLUTION_ID, 1)
        bundle.putString(AnTongVideoView.EXTRA_ID, "")
        bundle.putString(AnTongVideoView.PIN_CODE, "")
        bundle.putString(AnTongVideoView.PLAY_TOKEN, "")
        bundle.putString(AnTongVideoView.APP_CHANNEL, APP_CHANNEL)
        bundle.putBoolean(AnTongVideoView.IS_PORTRAIT, false)
        bundle.putString(AnTongVideoView.BUSINESS_GAME_ID, gameId)
        bundle.putString(AnTongVideoView.SIGN, sign)
        bundle.putInt(AnTongVideoView.NO_INPUT_TIMEOUT, 50 * 60)
        bundle.putString(AnTongVideoView.PKG_NAME, anTongPackageName)
        anTongVideoView?.play(bundle)
    }

    private fun getProtoData(userId: String, gameId: String): String {
        val protoJSONObject = JSONObject()
        protoJSONObject.put("uid", userId)
        protoJSONObject.put("gameId", gameId)
        protoJSONObject.put("type", 2)
        val toString = protoJSONObject.toString()
        val byteArray = toString.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun stopGame() {
        anTongVideoView?.stopGame()
    }

    fun stopGame(code: Int) {
//        anTongVideoView?.stopGame(code)
    }

    fun onDestroy() {
        anTongVideoView?.setHmcpPlayerListener(null)
        anTongVideoView?.onDestroy()
        val parentViewGroup = anTongVideoView?.parent as? ViewGroup
        if (parentViewGroup != null && anTongVideoView != null) {
            parentViewGroup.removeView(anTongVideoView)
        }
        anTongVideoView = null
    }

    fun leaveQueue() {
        anTongVideoView?.setHmcpPlayerListener(null)
        val leaveQueue = anTongVideoView?.leaveQueue() ?: true
        if (leaveQueue) {
            onDestroy()
        }
    }
}