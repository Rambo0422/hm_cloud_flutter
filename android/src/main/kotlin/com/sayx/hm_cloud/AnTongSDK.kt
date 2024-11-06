package com.sayx.hm_cloud

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import com.media.atkit.AnTongManager
import com.media.atkit.Constants
import com.media.atkit.beans.UserInfo
import com.media.atkit.listeners.AnTongPlayerListener
import com.media.atkit.utils.StatusCallbackUtil
import com.media.atkit.widgets.AnTongVideoView
import com.sayx.hm_cloud.callback.RequestDeviceSuccess
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
                        anTongVideoView?.setHmcpPlayerListener(null)
                        // 跳转远程页面
                        mRequestDeviceSuccess?.onRequestDeviceSuccess()

                        // 首帧出现，修改码率
                        anTongVideoView?.onSwitchResolution(1)
                    }

                    Constants.STATUS_APP_ID_ERROR,
                    Constants.STATUS_NOT_FOND_GAME,
                    Constants.STATUS_SIGN_FAILED,
                    Constants.STATUS_201003 -> {
                        val errorMessage =
                            jsonObject.optString(StatusCallbackUtil.DATA, "服务器异常")
                        mRequestDeviceSuccess?.onRequestDeviceFailed(errorMessage)
                    }

                    else -> {}
                }
            } ?: return
        }

        override fun onPlayerError(errorCode: String, errorInfo: String) {
            mRequestDeviceSuccess?.onRequestDeviceFailed(errorInfo)
        }
    }

    fun initSdk(context: Context, channelName: String, accessKeyId: String) {
        Constants.IS_DEBUG = BuildConfig.DEBUG
        Constants.IS_ERROR = BuildConfig.DEBUG
        Constants.IS_INFO = BuildConfig.DEBUG
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
        bundle.putInt(AnTongVideoView.NO_INPUT_TIMEOUT, 5 * 60)
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

    fun onDestroy() {
        anTongVideoView?.onDestroy()
        val parentViewGroup = anTongVideoView?.parent as? ViewGroup
        if (parentViewGroup != null && anTongVideoView != null) {
            parentViewGroup.removeView(anTongVideoView)
        }
        anTongVideoView = null
    }

    fun leaveQueue() {
        val leaveQueue = anTongVideoView?.leaveQueue() ?: true
        if (leaveQueue) {
            onDestroy()
        }
    }
}